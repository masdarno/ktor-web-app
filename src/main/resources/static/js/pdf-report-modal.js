document.addEventListener("click", (e) => {
    const trigger = e.target.closest("[data-pdf-report-url]");
    if (!trigger) return;

    const modalId = trigger.dataset.pdfModalTarget || "pdfModal";
    const modalEl = document.getElementById(modalId);
    if (!modalEl) return;

    const frame = modalEl.querySelector(".pdf-frame");
    const loading = modalEl.querySelector(".pdf-loading");
    const error = modalEl.querySelector(".pdf-error");

    const showLoading = () => {
        loading.classList.replace("d-none", "d-flex");
        error.classList.replace("d-flex", "d-none");
    };
    const showSuccess = () => {
        loading.classList.replace("d-flex", "d-none");
        error.classList.replace("d-flex", "d-none");
    };
    const showError = () => {
        loading.classList.replace("d-flex", "d-none");
        error.classList.replace("d-none", "d-flex");
    };

    // Ambil daftar param dari data-attribute, format: "name:selector,name2:selector2"
    const paramConfig = (trigger.dataset.pdfParams || "")
        .split(",")
        .filter(Boolean)
        .map(pair => pair.split(":"));

    const params = new URLSearchParams();
    paramConfig.forEach(([name, selector]) => {
        const el = document.querySelector(selector);
        const value = el ? el.value.trim() : "";
        if (value) params.set(name, value);
    });

    const url = trigger.dataset.pdfReportUrl +
        (params.toString() ? "?" + params.toString() : "");

    showLoading();
    frame.src = "";

    const modal = coreui.Modal.getOrCreateInstance(modalEl);
    modal.show();

    requestAnimationFrame(() => { frame.src = url; });

    frame.onload = showSuccess;
    modalEl.addEventListener("hidden.coreui.modal", () => {
        frame.src = "";
        showLoading();
    }, { once: true });
});