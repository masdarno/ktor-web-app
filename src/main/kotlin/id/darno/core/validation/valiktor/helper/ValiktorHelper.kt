package id.darno.core.validation.valiktor.helper

import org.valiktor.ConstraintViolationException
import org.valiktor.i18n.mapToMessage
import java.util.*

fun ConstraintViolationException.errors(
    baseName: String = "messages",
    locale: Locale = Locale.forLanguageTag("id"),
    useSimplePropertyName: Boolean = true
): Map<String, String> {
    return this.constraintViolations
        .mapToMessage(baseName = baseName, locale = locale)
        .associate { violation ->
            val property = if (useSimplePropertyName) {
                violation.property.substringAfterLast('.')
            } else {
                violation.property
            }
            val errorMessage = violation.message
            property to errorMessage
        }
}