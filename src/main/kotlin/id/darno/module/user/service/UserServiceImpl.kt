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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.SimpleJasperReportsContext
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.export.SimpleExporterInput
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput
import net.sf.jasperreports.pdf.JRPdfExporter
import net.sf.jasperreports.repo.InputStreamResource
import net.sf.jasperreports.repo.RepositoryService
import net.sf.jasperreports.repo.Resource
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream

class UserServiceImpl(
    private val userRepository: UserRepository,
    private val roleService: RoleService,
    private val hasher: Hasher
) : UserService {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    // --- CREATE ---
    override suspend fun create(params: CreateUserParams): UserDomain {
        logger.info("Create user with name: {}", params.nama)

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

    companion object {
        private const val REPORT_PATH = "reports/users.jasper"
    }

    override suspend fun generateUsersPdf(search: String?): ByteArray = withContext(Dispatchers.IO) {
        val rows = userRepository.findAllForReport(search)
        val dataSource = JRBeanCollectionDataSource(rows)

        val classLoader = Thread.currentThread().contextClassLoader ?: javaClass.classLoader

        // 1. Inisialisasi Context Kustom
        val context = SimpleJasperReportsContext()

        // 2. RepositoryService kustom dengan penanganan Kotlin nullability yang valid
        val classLoaderRepository = object : RepositoryService {

            @Suppress("UNCHECKED_CAST")
            override fun <T : Resource?> getResource(location: String?, javaType: Class<T>?): T {
                val resource = getResource(location)
                if (resource != null && (javaType == null || javaType.isInstance(resource))) {
                    return resource as T
                }
                return null as T
            }

            override fun getResource(location: String?): Resource? {
                // Perbaikan typo di baris ini:
                if (location.isNullOrEmpty()) return null

                // Petakan path relatif "shared/MY_STYLES.jrtx" menjadi "reports/shared/MY_STYLES.jrtx"
                val resourcePath = if (location.startsWith("shared/")) {
                    "reports/$location"
                } else {
                    location
                }

                val stream = classLoader.getResourceAsStream(resourcePath) ?: return null
                val res = InputStreamResource()
                res.inputStream = stream
                return res
            }

            override fun saveResource(location: String?, resource: Resource?) {}
        }

        // 3. Daftarkan Service ke Context
        context.setExtensions(RepositoryService::class.java, listOf(classLoaderRepository))

        // 4. Load file compiled .jasper
        val reportStream = classLoader.getResourceAsStream(REPORT_PATH)
        ?: throw IllegalStateException("Resource laporan tidak ditemukan di path: $REPORT_PATH")

        val parameters = HashMap<String, Any>()

        // 5. Fill Report menggunakan FillManager berbasis Context kustom
        val filler = JasperFillManager.getInstance(context)
        val jasperPrint = filler.fill(reportStream, parameters, dataSource)

        // 6. Export ke PDF
        val exporter = JRPdfExporter(context)
        exporter.setExporterInput(SimpleExporterInput(jasperPrint))

        val outputStream = ByteArrayOutputStream()
        exporter.setExporterOutput(SimpleOutputStreamExporterOutput(outputStream))
        exporter.exportReport()

        outputStream.toByteArray()
    }
}