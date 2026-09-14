package id.darno.core.validation.rules

import id.darno.core.validation.FieldValidator

fun <T> FieldValidator.notEmpty(
    value: Collection<T>,
    field: String,
    message: String = "minimal harus memilih satu"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it.isNotEmpty()
    }
}

fun <T> FieldValidator.minSize(
    value: Collection<T>,
    field: String,
    min: Int,
    message: String = "minimal $min item"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it.size >= min
    }
}

fun <T> FieldValidator.maxSize(
    value: Collection<T>,
    field: String,
    max: Int,
    message: String = "maksimal $max item"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it.size <= max
    }
}