package id.darno.core.validation

interface FormValidator<in T> {

    fun validate(value: T): List<FieldError>
}