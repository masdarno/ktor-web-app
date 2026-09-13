package id.darno.core.http.mapper

import io.ktor.http.*
import io.ktor.util.*

fun Parameters.toFormData(): Map<String, String> =
    toMap().mapNotNull { (key, values) ->
        values.firstOrNull()?.let { key to it }
    }.toMap()

/**
 * Sama seperti toFormData(), tapi memungkinkan menambahkan/override
 * field tambahan yang tidak berasal dari HTTP form (misal: id dari path parameter,
 * atau field lain dengan tipe non-String seperti Short, Int, Boolean).
 *
 * Semua value pada overrides otomatis dikonversi ke String.
 * Jika key sudah ada di parameter form, value dari overrides akan menang (override).
 */
fun Parameters.toFormData(vararg overrides: Pair<String, Any?>): Map<String, String> {
    val base = toFormData()
    val overrideMap = overrides.associate { (key, value) ->
        key to (value?.toString() ?: "")
    }
    return base + overrideMap
}