package id.darno.dev.cli

import id.darno.core.database.DatabaseProvider
import id.darno.core.database.databaseConfig
import id.darno.core.security.crypto.BCryptHasher
import id.darno.module.user.config.userPhotoUrlConfig
import id.darno.module.user.repository.UserProvisioningRepositoryImpl
import id.darno.module.user.repository.UserRepositoryImpl
import id.darno.module.user.service.UserProvisioningServiceImpl
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.config.*
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    val dotenv = dotenv {
        ignoreIfMissing = true
    }

    dotenv.entries().forEach {
        System.setProperty(it.key, it.value)
    }

    val config = ApplicationConfig("application.yaml")

    // --- Parse & validasi argumen CLI ---
    if (args.size != 3) {
        println(
            """
            Usage:
              ./gradlew addUser --args="<username> <roleId> <unitId>"

            Example:
              ./gradlew addUser --args="budi 1 2"
              ./gradlew addUser --args="wati 1 2" --configuration-cache
            """.trimIndent()
        )
        return
    }

    val username = args[0]
    val roleId = args[1].toShortOrNull()
    val unitId = args[2].toShortOrNull()

    if (roleId == null || unitId == null) {
        println("roleId dan unitId harus berupa angka (Short)")
        return
    }

    val dbConfig = config.databaseConfig()
    val dbProvider = DatabaseProvider(dbConfig)

    try {
        dbProvider.connect()

        runBlocking {
            // --- Provisioning user ---

            val photoUrlConfig = config.userPhotoUrlConfig()

            val hasher = BCryptHasher()

            val userRepository = UserRepositoryImpl(photoUrlConfig)
            val provisioningRepository = UserProvisioningRepositoryImpl(photoUrlConfig)

            val provisioningService = UserProvisioningServiceImpl(
                userRepository = userRepository,
                provisioningRepository = provisioningRepository,
                hasher = hasher
            )

            val cliService = AddUserCliService(provisioningService)

            try {
                cliService.execute(
                    username = username,
                    roleId = roleId,
                    unitId = unitId
                )
            } catch (ex: IllegalArgumentException) {
                println()
                println("GAGAL: ${ex.message}")
            } catch (ex: Exception) {
                println()
                println("GAGAL: ${ex.message ?: "Terjadi kesalahan tak terduga"}")
            }
        }
    } finally {
        dbProvider.close()
    }
}