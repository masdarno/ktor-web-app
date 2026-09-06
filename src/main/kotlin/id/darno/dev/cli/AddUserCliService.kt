package id.darno.dev.cli

import id.darno.core.model.DefaultValues
import id.darno.module.user.model.CreateUserParams
import id.darno.module.user.service.UserProvisioningService

class AddUserCliService(
    private val userProvisioningService: UserProvisioningService
) {

    suspend fun execute(
        username: String,
        roleId: Short,
        unitId: Short
    ) {

        val cleanUsername = username
            .trim()
            .lowercase()

        if (cleanUsername.isBlank()) {
            throw IllegalArgumentException(
                "Username tidak boleh kosong"
            )
        }

        if (cleanUsername.length !in 3..10) {
            throw IllegalArgumentException(
                "Username harus 3-10 karakter"
            )
        }

        if (roleId <= 0) {
            throw IllegalArgumentException(
                "roleId harus > 0"
            )
        }

        if (unitId <= 0) {
            throw IllegalArgumentException(
                "unitId harus > 0"
            )
        }

        val nama = cleanUsername
            .replaceFirstChar {
                if (it.isLowerCase()) {
                    it.titlecase()
                } else {
                    it.toString()
                }
            }

        val params = CreateUserParams(
            nama = nama,
            alias = nama,
            username = cleanUsername,

            // Development:
            // password = username
            password = cleanUsername,

            email = "$cleanUsername@localhost",

            photo = DefaultValues.DEFAULT_PHOTO,
            genderId = DefaultValues.DEFAULT_GENDER,
            roleId = roleId
        )

        val user =
            userProvisioningService.createUserWithUnit(
                params = params,
                unitId = unitId
            )

        println()
        println("================================")
        println(" USER BERHASIL DIBUAT")
        println("================================")
        println("ID       : ${user.id}")
        println("Nama     : ${user.nama}")
        println("Username : ${user.username}")
        println("Password : ${cleanUsername}")
        println("Email    : ${user.email}")
        println("Role ID  : ${user.roleId}")
        println("Unit ID  : $unitId")
        println("================================")
    }
}