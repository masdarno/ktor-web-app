package id.darno.core.pageddata.model

data class PagedResult<T>(
    val data: List<T>,
    val page: Int,
    val pageSize: Int,
    val total: Long,
    val totalPages: Int
)