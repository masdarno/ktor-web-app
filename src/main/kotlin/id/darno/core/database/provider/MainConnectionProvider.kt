package id.darno.core.database.provider

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import id.darno.core.database.config.DatabaseConfig
import org.jetbrains.exposed.v1.jdbc.Database

class MainConnectionProvider(private val databaseConfig: DatabaseConfig) : AutoCloseable {
    // dataSource akan diinisialisasi saat pertama kali diakses
    private val dataSource: HikariDataSource by lazy {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = databaseConfig.jdbcUrl
            username = databaseConfig.user
            password = databaseConfig.password
            driverClassName = databaseConfig.driver
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        databaseConfig.type.applyTuning(hikariConfig)
        HikariDataSource(hikariConfig)
    }

    fun connect() {
        Database.connect(dataSource) // dataSource akan diinisialisasi di sini
    }

    override fun close() {
        dataSource.close()
    }
}
