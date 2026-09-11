package id.darno.core.database.config

data class DatabaseConfig(
    val type: JdbcType,
    val host: String,
    val port: Int?,
    val name: String,
    val user: String,
    val password: String
) {
    val jdbcUrl: String
        get() = "${type.prefix}://${host}:${port ?: type.defaultPort}/${name}"

    val driver: String
        get() = type.driver

    // Validasi tambahan setelah objek dibuat
    init {
        require(host.isNotBlank()) { "Database host cannot be blank" }
        require(name.isNotBlank()) { "Database name cannot be blank" }
        require(user.isNotBlank()) { "Database user cannot be blank" }
        // password boleh kosong di beberapa kasus (misalnya trust auth Postgres), jadi tidak wajib
        if (port != null) require(port in 1..65535) { "Port must be between 1 and 65535" }
    }
}