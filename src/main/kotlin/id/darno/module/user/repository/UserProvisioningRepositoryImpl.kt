package id.darno.module.user.repository

import id.darno.core.database.DbExceptionMapper
import id.darno.core.database.dbQuery
import id.darno.module.role.database.dao.RoleEntity
import id.darno.module.unit.database.dao.UnitEntity
import id.darno.module.user.config.PhotoUrlConfig
import id.darno.module.user.database.dao.UserEntity
import id.darno.module.user.database.table.UserUnitTable
import id.darno.module.user.domain.UserDomain
import id.darno.module.user.mapper.toUserDomain
import id.darno.module.user.model.CreateUserParams
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.insert

class UserProvisioningRepositoryImpl(
    private val config: PhotoUrlConfig
) : UserProvisioningRepository {

    override suspend fun createUserWithUnit(
        params: CreateUserParams,
        unitId: Short
    ): UserDomain = dbQuery {

        try {
            /*
             * Satu dbQuery = satu transaction.
             *
             * Jadi:
             *
             * INSERT users
             * INSERT user_units
             *
             * harus sama-sama berhasil.
             *
             * Jika INSERT user_units gagal,
             * INSERT users juga rollback.
             */

            // Pastikan FK unit valid.
            val unit = UnitEntity[unitId]

            // 1. INSERT users
            val user = UserEntity.new {

                nama = params.nama
                alias = params.alias
                username = params.username
                password = params.password
                email = params.email
                genderId = params.genderId
                photo = params.photo

                role = RoleEntity[params.roleId]
            }

            // 2. INSERT user_units
            UserUnitTable.insert {
                it[UserUnitTable.userId] = user.id
                it[UserUnitTable.unitId] = unit.id
            }

            // Kembalikan domain user.
            user.toUserDomain(config)

        } catch (e: ExposedSQLException) {
            throw DbExceptionMapper.map(e)
        }
    }
}