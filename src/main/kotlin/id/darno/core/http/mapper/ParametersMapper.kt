package id.darno.core.http.mapper

import io.ktor.http.*
import io.ktor.util.*

fun Parameters.toFormData(): Map<String, String> =
    toMap().mapNotNull { (key, values) ->
        values.firstOrNull()?.let { key to it }
    }.toMap()