package id.darno.core.pageddata.model

data class PagedQuery(
    val search: String?,
    val page: Int,
    val pageSize: Int,
    val sortBy: String,
    val sortDir: String
)