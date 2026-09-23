package id.darno.core.validation

fun List<FieldError>.toErrorMap(): Map<String, String> =
    associate { error ->
        error.field to error.message
    }