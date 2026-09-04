package id.darno.core.validation.valiktor.constraint

import org.valiktor.Constraint
import org.valiktor.Validator

// --- Custom Constraint ---
data class PasswordsMustMatch(val value: String) : Constraint {
    override val messageParams: Map<String, *>
        get() = emptyMap<String, Any>()
}
data class PasswordsMustNotMatch(val value: String) : Constraint {
    override val messageParams: Map<String, *>
        get() = emptyMap<String, Any>()
}


// --- Extension Function ---
fun <E> Validator<E>.Property<String?>.isEqualToPassword(password: String): Validator<E>.Property<String?> =
    this.validate(PasswordsMustMatch(password)) {
        it == null || it == password
    }
fun <E> Validator<E>.Property<String?>.isNotEqualToPassword(password: String): Validator<E>.Property<String?> =
    this.validate(PasswordsMustNotMatch(password)) {
        it == null || it != password
    }
