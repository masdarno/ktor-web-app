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

    // Parsing param config — mendukung format satu baris maupun multiline
    const paramConfig = (trigger.dataset.pdfParams || "")
        .split(",")
        .map(s => s.trim())
        .filter(Boolean)
        .map(pair => pair.split(":").map(s => s.trim()));

    const params = new URLSearchParams();

    paramConfig.forEach(([name, selector]) => {
        const elements = document.querySelectorAll(selector);
        if (elements.length === 0) return;

        const first = elements[0];
        const type = (first.type || "").toLowerCase();

        if (type === "radio") {
            const checked = document.querySelector(`${selector}:checked`);
            if (checked) params.set(name, checked.value.trim());

        } else if (type === "checkbox") {
            const checkedValues = Array.from(elements)
                .filter(el => el.checked)
                .map(el => el.value.trim());

            if (checkedValues.length === 1) {
                params.set(name, checkedValues[0]);
            } else if (checkedValues.length > 1) {
                checkedValues.forEach(v => params.append(name, v));
            }

        } else {
            const value = first.value.trim();
            if (value) params.set(name, value);
        }
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