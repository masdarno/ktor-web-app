package id.darno.core.database

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.QueryBuilder

interface TimeExpressions {
    companion object {
        // Ekspresi CURRENT_TIMESTAMP untuk kotlinx.datetime
        val CurrentKotlinDateTime = object : Expression<LocalDateTime>() {
            override fun toQueryBuilder(queryBuilder: QueryBuilder) {
                queryBuilder.append("CURRENT_TIMESTAMP")
            }
        }
    }
}
