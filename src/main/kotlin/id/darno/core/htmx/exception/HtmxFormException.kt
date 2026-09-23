package id.darno.core.htmx.exception

class HtmxFormException(
    val templatePath: String,
    val errors: Map<String, String>,
    val formData: Map<String, String>,
    val formElement: Map<String, Any>? = null,
    val mode: String = "add" // add -> hx-post, edit -> hx-put
) : RuntimeException("Form validation failed")
