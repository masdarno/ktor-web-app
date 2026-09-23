package id.darno.core.validation.rules

import id.darno.core.validation.FieldValidator

fun FieldValidator.min(
    value: Int,
    field: String,
    min: Int,
    message: String = "minimal $min"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it >= min
    }
}

fun FieldValidator.max(
    value: Int,
    field: String,
    max: Int,
    message: String = "maksimal $max"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it <= max
    }
}

fun FieldValidator.between(
    value: Int,
    field: String,
    range: IntRange,
    message: String =
        "nilai harus di antara ${range.first}-${range.last}"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it in range
    }
}

fun FieldValidator.positive(
    value: Int,
    field: String,
    message: String = "harus lebih besar dari 0"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it > 0
    }
}

fun FieldValidator.nonNegative(
    value: Int,
    field: String,
    message: String = "tidak boleh negatif"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it >= 0
    }
}