package id.darno.core.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database

class DatabaseProvider(private val dbConfig: DatabaseConfig) : AutoCloseable {
    // dataSource akan diinisialisasi saat pertama kali diakses
    private val dataSource: HikariDataSource by lazy {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = dbConfig.jdbcUrl
            username = dbConfig.user
            password = dbConfig.password
            driverClassName = dbConfig.driver
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        dbConfig.type.applyTuning(hikariConfig)
        HikariDataSource(hikariConfig)
    }

    fun connect() {
        Database.connect(dataSource) // dataSource akan diinisialisasi di sini
    }

    override fun close() {
        dataSource.close()
    }
}
