package id.darno.module.user

import id.darno.core.security.crypto.Hasher
import id.darno.core.storage.FileStorageService
import id.darno.module.role.service.RoleService
import id.darno.module.unit.service.UnitService
import id.darno.module.user.config.userModuleConfig
import id.darno.module.user.controller.UserController
import id.darno.module.user.controller.UserProfileController
import id.darno.module.user.controller.UserUnitController
import id.darno.module.user.repository.UserRepository
import id.darno.module.user.repository.UserRepositoryImpl
import id.darno.module.user.service.*
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

fun Application.configureUserDependencies(){
    val userConfig = userModuleConfig()
    dependencies {
        provide<UserRepository> {
            UserRepositoryImpl(userConfig.photoUrl)
        }
        provide<UserAuthService> {
            UserAuthServiceImpl(
                resolve<UserService>(),
                resolve<UserRepository>(),
                resolve<Hasher>("bcrypt")
            )
        }
        provide<UserEmailVerificationService> {
            UserEmailVerificationServiceImpl(resolve<UserRepository>())
        }
        provide<UserFileService> {
            UserFileServiceImpl(
                userConfig.upload,
                resolve<FileStorageService>()
            )
        }
        provide<UserLookupService> {
            UserLookupServiceImpl(resolve<UserService>())
        }
        provide<UserService> {
            UserServiceImpl(
                resolve<UserRepository>(),
                resolve<RoleService>(),
                resolve<Hasher>("bcrypt")
            )
        }
        provide<UserController> {
            UserController(
                resolve<UserService>(),
                resolve<RoleService>()
            )
        }
        provide<UserProfileController> {
            UserProfileController(
                resolve<UserService>(),
                resolve<UserFileService>()
            )
        }
        provide< UserUnitController> {
            UserUnitController(
                resolve<UserService>(),
                resolve<UnitService>()
            )
        }
    }
}