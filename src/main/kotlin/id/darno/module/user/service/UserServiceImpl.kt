package id.darno.module.user.service

import id.darno.core.exceptions.service.ConflictException
import id.darno.core.exceptions.service.NotFoundException
import id.darno.core.pageddata.model.PagedQuery
import id.darno.core.security.crypto.Hasher
import id.darno.module.role.service.RoleService
import id.darno.module.unit.domain.UnitDomain
import id.darno.module.user.domain.UserDomain
import id.darno.module.user.model.CreateUserParams
import id.darno.module.user.model.UpdateUserParams
import id.darno.module.user.repository.UserRepository
import org.slf4j.LoggerFactory

class UserServiceImpl(
    private val userRepository: UserRepository,
    private val roleService: RoleService,
    private val hasher: Hasher
) : UserService {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    // --- CREATE ---
    override suspend fun create(params: CreateUserParams): UserDomain {
        logger.info("Create user with name: {}", params.name)

        if (userRepository.existsByUsername(params.username))
            throw ConflictException("Username ${params.username} sudah ada")

        if(userRepository.existsByEmail(params.email))
            throw ConflictException("Email ${params.email} sudah ada")

        params.roleId?.let {
            roleService.getById(it) // Validasi: akan throw NotFoundException jika role tidak ada
        }

        val hashedPassword = hasher.hash(params.password)

        val secureParams = params.copy(
            password = hashedPassword
        )

        return userRepository.create(secureParams)
    }

    // --- READ ---
    override suspend fun getById(id: Short): UserDomain {
        return userRepository.findById(id)
            ?: throw NotFoundException("User tidak ditemukan")
    }

    override suspend fun getByEmail(email: String): UserDomain? {
        return userRepository.findByEmail(email)
    }

    // --- UPDATE ---
    override suspend fun update(id: Short, params: UpdateUserParams): UserDomain {
        // 1. Cek user exists
        val existingUser = userRepository.findById(id)
            ?: throw NotFoundException("User tidak ditemukan")
        // 2. Validasi role jika ada
        params.roleId?.let {
            roleService.getById(it)// dari RoleService sudah ?: throw BadRequestException("Role not found")
        }
        // 3. Cek username duplikat (jika diubah)
        params.username?.let { newUsername ->
            if (newUsername != existingUser.username) {
                if (userRepository.existsByUsername(newUsername))
                    throw ConflictException("Username $newUsername sudah ada")
            }
        }
        // 4. Cek email duplikat (jika diubah)
        params.email?.let { newEmail ->
            if (newEmail != existingUser.email) {
                if(userRepository.existsByEmail(newEmail))
                    throw ConflictException("Email $newEmail sudah ada")
            }
        }
        // 5. Password
        val hashedPassword = params.password?.let{
            hasher.hash(it)
        }

        val secureParams = params.copy(
            password = hashedPassword
        )
        // 6. Update User setelah dipastikan user exists
        return userRepository.update(id, secureParams)
    }

    // --- DELETE ---
    override suspend fun delete(id: Short): Boolean {
        // Pastiin dulu id ada
        userRepository.findById(id)
            ?: throw NotFoundException("User tidak ditemukan")

        // Baru dihapus
        return userRepository.delete(id)
    }

    // --- USER_UNIT ---
    override suspend fun getUnitsForUser(userId: Short): List<UnitDomain> {
        // Pastiin dulu userId ada
        userRepository.findById(userId)
            ?: throw NotFoundException("User tidak ditemukan")

        return userRepository.findUnitsByUserId(userId)
    }

    override suspend fun userHasUnit(userId: Short, unitId: Short): Boolean {
        return userRepository.userHasUnit(userId, unitId)
    }

    override suspend fun getUserTable(query: PagedQuery) =
        userRepository.findAll(
            search = query.search,
            page = query.page,
            pageSize = query.pageSize,
            sortBy = query.sortBy,
            sortDir = query.sortDir
        )

}