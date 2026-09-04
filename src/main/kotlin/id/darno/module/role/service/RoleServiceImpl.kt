package id.darno.module.role.service

import id.darno.core.exceptions.service.ConflictException
import id.darno.module.role.domain.RoleDomain
import id.darno.module.role.model.RoleCreateParams
import id.darno.module.role.model.RoleUpdateParams
import id.darno.module.role.repository.RoleRepository
import io.ktor.server.plugins.*

class RoleServiceImpl(
    private val roleRepository: RoleRepository
) : RoleService {

    override suspend fun getAll(): List<RoleDomain> =
        roleRepository.findAll()

    override suspend fun getById(id: Short): RoleDomain =
        roleRepository.findById(id)
            ?: throw NotFoundException("Role tidak ditemukan")

    override suspend fun create(params: RoleCreateParams): RoleDomain {
        if(roleRepository.existsByName(params.nama))
            throw ConflictException("Role '${params.nama}' sudah ada")

        return roleRepository.create(params)
    }

    override suspend fun update(id: Short, params: RoleUpdateParams): RoleDomain {
        val role = roleRepository.findById(id)
            ?: throw NotFoundException("Role tidak ditemukan")

        params.nama?.let { newName ->
            if (newName != role.name) {
                if(roleRepository.existsByName(newName))
                    throw ConflictException("Role '$newName' sudah ada")
            }
        }

        return roleRepository.update(id, params)
    }

    override suspend fun delete(id: Short): Boolean {
        roleRepository.findById(id)
            ?: throw NotFoundException("Role tidak ditemukan")

        return roleRepository.delete(id)
    }
}