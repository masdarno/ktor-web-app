package id.darno.core.database

import id.darno.core.database.config.reportingDatabaseConfig
import id.darno.core.database.provider.ReportingConnectionProvider
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

fun Application.configureReportingDatabase() {
    val databaseConfig = reportingDatabaseConfig()
    dependencies {
        provide<ReportingConnectionProvider> { ReportingConnectionProvider(databaseConfig) }
    }
    val provider: ReportingConnectionProvider by dependencies

    // Tidak perlu provider.connect()

    monitor.subscribe(ApplicationStopped) {
        log.info("Closing reporting connection pool...")
        provider.close()
    }
}