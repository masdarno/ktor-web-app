package id.darno.core.database

import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

fun Application.configureDatabase() {
    val dbConfig = databaseConfig()
    dependencies {
        provide<DatabaseProvider> { DatabaseProvider(dbConfig) }
    }
    val dbProvider: DatabaseProvider by dependencies
    dbProvider.connect()

    // Monitor lifecycle Ktor
    monitor.subscribe(ApplicationStopped) {
        log.info("Closing database connections...")
        dbProvider.close()
    }
}