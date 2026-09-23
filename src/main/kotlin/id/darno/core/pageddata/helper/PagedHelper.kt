package id.darno.core.pageddata.helper

import id.darno.core.pageddata.model.PagedQuery
import io.ktor.server.application.*

fun ApplicationCall.pagedQueryParameters(defaultPage: Int = 1) =
    PagedQuery(
        search = request.queryParameters["search"]?.takeIf { it.isNotBlank() },
        page = request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: defaultPage,
        pageSize = request.queryParameters["pageSize"]
            ?.toIntOrNull()
            ?.coerceIn(5, 100) ?: 10,
        sortBy = request.queryParameters["sortBy"]
            ?: "name",
        sortDir = request.queryParameters["sortDir"]
            ?.lowercase()
            ?.takeIf { it in setOf("asc", "desc") }
            ?: "asc"
    )