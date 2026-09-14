package id.darno.core.validation.rules

import id.darno.core.validation.FieldValidator

fun FieldValidator.notBlank(
    value: String,
    field: String,
    message: String = "tidak boleh kosong"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it.isNotBlank()
    }
}

fun FieldValidator.notEmpty(
    value: String,
    field: String,
    message: String = "tidak boleh kosong"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it.isNotEmpty()
    }
}

fun FieldValidator.minLength(
    value: String,
    field: String,
    min: Int,
    message: String = "minimal $min karakter"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it.length >= min
    }
}

fun FieldValidator.maxLength(
    value: String,
    field: String,
    max: Int,
    message: String = "maksimal $max karakter"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it.length <= max
    }
}

fun FieldValidator.lengthBetween(
    value: String,
    field: String,
    range: IntRange,
    message: String =
        "panjang harus di antara ${range.first}-${range.last} karakter"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it.length in range
    }
}

fun FieldValidator.matches(
    value: String,
    field: String,
    regex: Regex,
    message: String
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        regex.matches(it)
    }
}

fun FieldValidator.email(
    value: String,
    field: String,
    message: String = "format email tidak valid"
) {
    matches(
        value = value,
        field = field,
        regex = EMAIL_REGEX,
        message = message
    )
}

fun FieldValidator.equalsTo(
    value: String,
    field: String,
    expected: String,
    message: String = "nilai tidak sama"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it == expected
    }
}

fun FieldValidator.notEqualsTo(
    value: String,
    field: String,
    expected: String,
    message: String = "nilai tidak boleh sama"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it != expected
    }
}

fun FieldValidator.oneOf(
    value: String,
    field: String,
    allowed: Collection<String>,
    message: String =
        "harus salah satu dari: ${allowed.joinToString()}"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it in allowed
    }
}

private val EMAIL_REGEX =
    Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")