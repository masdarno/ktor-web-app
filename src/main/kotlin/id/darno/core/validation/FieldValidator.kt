package id.darno.core.validation

class FieldValidator(
    private val prefix: String = ""
) {
    private val _errors = mutableListOf<FieldError>()

    val errors: List<FieldError>
        get() = _errors

    val hasErrors: Boolean
        get() = _errors.isNotEmpty()

    private fun fieldName(field: String): String =
        if (prefix.isEmpty()) field else "$prefix.$field"

    fun <T> check(
        value: T,
        field: String,
        message: String,
        predicate: (T) -> Boolean
    ) {
        if (!predicate(value)) {
            _errors += FieldError(
                field = fieldName(field),
                message = message
            )
        }
    }

    fun checkIf(
        condition: Boolean,
        field: String,
        message: String,
        predicate: () -> Boolean
    ) {
        if (condition && !predicate()) {
            _errors += FieldError(
                field = fieldName(field),
                message = message
            )
        }
    }

    fun <T> nested(
        field: String,
        value: T,
        block: FieldValidator.(T) -> Unit
    ) {
        val validator = FieldValidator(fieldName(field))

        validator.block(value)

        _errors += validator.errors
    }

    fun <T> nestedList(
        field: String,
        values: List<T>,
        block: FieldValidator.(T) -> Unit
    ) {
        values.forEachIndexed { index, value ->
            val validator = FieldValidator(
                "${fieldName(field)}[$index]"
            )

            validator.block(value)

            _errors += validator.errors
        }
    }
}