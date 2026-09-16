package id.darno.module.user.service

import id.darno.core.exceptions.repository.DuplicateKeyException
import id.darno.core.exceptions.repository.ForeignKeyException
import id.darno.core.exceptions.service.NotFoundException
import id.darno.core.pageddata.model.PagedQuery
import id.darno.core.report.JasperReportService
import id.darno.core.report.model.ReportFile
import id.darno.core.report.model.ReportFormat
import id.darno.core.security.crypto.Hasher
import id.darno.module.role.service.RoleService
import id.darno.module.unit.domain.UnitDomain
import id.darno.module.unit.repository.CompanyProfileRepository
import id.darno.module.user.domain.UserDomain
import id.darno.module.user.exception.UserException
import id.darno.module.user.exception.toUserException
import id.darno.module.user.model.CreateUserParams
import id.darno.module.user.model.UpdateUserParams
import id.darno.module.user.repository.UserRepository
import org.slf4j.LoggerFactory

class UserServiceImpl(
    private val userRepository: UserRepository,
    private val roleService: RoleService,
    private val hasher: Hasher,
    private val jasperReportService: JasperReportService,
    private val companyProfileRepository: CompanyProfileRepository
) : UserService {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    // --- CREATE ---
    override suspend fun create(params: CreateUserParams): UserDomain {
        logger.info("Create user with name: {}", params.nama)

        if (userRepository.existsByUsername(params.username))
            throw UserException.UsernameAlreadyExists(params.username)

        if (userRepository.existsByEmail(params.email))
            throw UserException.EmailAlreadyExists(params.email)

        params.roleId.let {
            try {
                roleService.getById(it)
            } catch (ex: NotFoundException) {
                throw UserException.RoleNotFound(it)
            }
        }

        val hashedPassword = hasher.hash(params.password)

        val secureParams = params.copy(
            password = hashedPassword
        )

        return try {
            userRepository.create(secureParams)
        }
        catch (e: ForeignKeyException) {
            throw e.toUserException(
                roleId = params.roleId,
                genderId = params.genderId
            )
        }
        catch (e: DuplicateKeyException) {
            throw e.toUserException(
                username = params.username,
                email = params.email
            )
        }
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

        // 2. Cek username duplikat (jika diubah)
        params.username?.let { newUsername ->
            if (newUsername != existingUser.username) {
                if (userRepository.existsByUsername(newUsername))
                    throw UserException.UsernameAlreadyExists(newUsername)
            }
        }
        // 3. Cek email duplikat (jika diubah)
        params.email?.let { newEmail ->
            if (newEmail != existingUser.email) {
                if(userRepository.existsByEmail(newEmail))
                    throw UserException.EmailAlreadyExists(newEmail)
            }
        }

        // 4. Validasi ada tidaknya role
        params.roleId?.let {
            try {
                roleService.getById(it)
            } catch (ex: NotFoundException) {
                throw UserException.RoleNotFound(it)
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
        return try {
            userRepository.update(id, secureParams)
        }
        catch (e: ForeignKeyException) {
            throw e.toUserException(
                roleId = params.roleId,
                genderId = params.genderId
            )
        }
        catch (e: DuplicateKeyException) {
            throw e.toUserException(
                username = params.username,
                email = params.email
            )
        }
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

    override suspend fun generateReport(
        search: String?,
        format: ReportFormat
    ): ReportFile {

        val rows = userRepository.findAllForReport(search)

        val kopSurat = companyProfileRepository.find()

        return jasperReportService.generate(
            reportPath = "reports/users.jasper",
            format = format,
            data = rows,
            fileName = "users",
            parameters = mapOf(
                "KOP_SURAT" to kopSurat
            )
        )
    }
    override suspend fun generateReportFromQuery(
        search: String?,
        format: ReportFormat
    ): ReportFile {
        val kopSurat = companyProfileRepository.find()

        val sql = """
        select a.nama, username, email,
               (case when email_verified_at is not null then 'Terverifikasi FQ' else '' end) verified,
               b.nama role
        from users a
        join roles b on a.role_id = b.id
        where (? is null or a.nama like concat('%', ?, '%'))
    """.trimIndent()

        return jasperReportService.generateFromQuery(
            reportPath = "reports/users.jasper",
            format = format,
            sql = sql,
            sqlParams = listOf(search, search),
            fileName = "users",
            parameters = mapOf("KOP_SURAT" to kopSurat)
        )
    }

    override suspend fun generateReportFromConnection(
        search: String?,
        format: ReportFormat
    ): ReportFile {
        val kopSurat = companyProfileRepository.find()

        return jasperReportService.generateFromConnection(
            reportPath = "reports/users.jasper",
            format = format,
            fileName = "users",
            parameters = mapOf(
                "KOP_SURAT" to kopSurat,
                "SEARCH_KEYWORD" to search   // ini yang dipakai $P{SEARCH_KEYWORD} di query jrxml
            )
        )
    }
}