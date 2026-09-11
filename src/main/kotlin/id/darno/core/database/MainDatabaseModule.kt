package id.darno.core.database

import id.darno.core.database.config.mainDatabaseConfig
import id.darno.core.database.provider.MainConnectionProvider
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

fun Application.configureMainDatabase() {
    val databaseConfig = mainDatabaseConfig()
    dependencies {
        provide<MainConnectionProvider> { MainConnectionProvider(databaseConfig) }
    }
    val provider: MainConnectionProvider by dependencies

    provider.connect()

    // Monitor lifecycle Ktor
    monitor.subscribe(ApplicationStopped) {
        log.info("Closing main database connections...")
        provider.close()
    }
}