package id.darno.core.database.provider

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import id.darno.core.database.config.DatabaseConfig
import javax.sql.DataSource

class ReportingConnectionProvider(private val databaseConfig: DatabaseConfig) : AutoCloseable {

    /**
     * dataSource sengaja diexpose sebagai val publik
     * (beda dari MainConnectionProvider yang private),
     * karena JasperReportService butuh ambil Connection langsung dari sini.
     * Sedangkan MainConnectionProvider tidak perlu expose apa pun
     * karena konsumennya cuma Exposed lewat Database.connect().
     */
    val dataSource: DataSource by lazy {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = databaseConfig.jdbcUrl
            username = databaseConfig.user
            password = databaseConfig.password
            driverClassName = databaseConfig.driver
            poolName = "reporting-pool"

            // Sengaja beda karakter dari pool aplikasi:
            maximumPoolSize = 4          // kecil, supaya tidak "makan" resource utama
            minimumIdle = 0
            isAutoCommit = true          // JDBC-langsung, bukan lewat Exposed transaction
            isReadOnly = true            // hint ke driver/DB kalau user memang read-only
            connectionTimeout = 10_000
            maxLifetime = 30 * 60_000L
            // Query report bisa lama -> beri keleluasaan
            validationTimeout = 5_000
        }
        databaseConfig.type.applyTuning(hikariConfig)
        HikariDataSource(hikariConfig)
    }

    override fun close() {
        (dataSource as? HikariDataSource)?.close()
    }
}