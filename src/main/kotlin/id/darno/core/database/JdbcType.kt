package id.darno.core.database

import com.zaxxer.hikari.HikariConfig

enum class JdbcType(
    val driver: String,
    val prefix: String,
    val defaultPort: Int
) {
    MARIADB(
        driver = "org.mariadb.jdbc.Driver",
        prefix = "jdbc:mariadb",
        defaultPort = 3306
    ) {
        override fun applyTuning(config: HikariConfig) {
            config.addDataSourceProperty("cachePrepStmts", "true")
            config.addDataSourceProperty("prepStmtCacheSize", "250")
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            config.addDataSourceProperty("useServerPrepStmts", "true")
        }
      },
    MYSQL(
        driver = "com.mysql.cj.jdbc.Driver",
        prefix = "jdbc:mysql",
        defaultPort = 3306
    ) {
        override fun applyTuning(config: HikariConfig) {
            config.addDataSourceProperty("cachePrepStmts", "true")
            config.addDataSourceProperty("prepStmtCacheSize", "250")
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            config.addDataSourceProperty("useServerPrepStmts", "true")
            config.addDataSourceProperty("useSSL", "false")
        }
      },
    POSTGRES(
        driver = "org.postgresql.Driver",
        prefix = "jdbc:postgresql",
        defaultPort = 5432
    ) {
        override fun applyTuning(config: HikariConfig) {
            config.addDataSourceProperty("reWriteBatchedInserts", "true")
            config.addDataSourceProperty("tcpKeepAlive", "true")
        }
    };

    abstract fun applyTuning(config: HikariConfig)

    companion object {
        fun fromString(value: String): JdbcType = try {
            valueOf(value.uppercase())
        } catch (e: IllegalArgumentException) {
            val validTypes = values().joinToString(", ") { it.name.lowercase() }
            throw IllegalArgumentException(
                "Invalid database type: '$value'. " +
                        "Supported types are: $validTypes"
            )
        }
    }
}