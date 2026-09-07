package id.darno.module.user.service

import id.darno.core.pageddata.model.PagedQuery
import id.darno.module.user.model.UserOptionItem
import id.darno.module.user.repository.UserUnitRepository
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
import java.io.ByteArrayOutputStream

class UserUnitServiceImpl(
    private val userUnitRepository: UserUnitRepository
): UserUnitService {
    override suspend fun getUserUnitTable(
        query: PagedQuery,
        unitId: Short
    ) =
        userUnitRepository.findAllUserByUnit(
            search = query.search,
            page = query.page,
            pageSize = query.pageSize,
            sortBy = query.sortBy,
            sortDir = query.sortDir,
            unitId = unitId
        )

    override suspend fun getAvailableUsersForUnit(
        unitId: Short,
        search: String?
    ): List<UserOptionItem> =
        userUnitRepository.findAvailableUserForUnit(unitId, search)

    override suspend fun addUsersToUnit(
        unitId: Short,
        userIds: List<Short>
    ): Int =
        userUnitRepository.addUserUnits(unitId, userIds)

    override suspend fun deleteUserUnit(
        userId: Short,
        unitId: Short
    ) =
        userUnitRepository.deleteUserUnit(
            userId = userId,
            unitId = unitId
        )

    companion object {
        private const val REPORT_PATH = "reports/user-unit.jasper"
    }

    override suspend fun generatePdf(unitId: Short, search: String?): ByteArray = withContext(Dispatchers.IO) {
        val rows = userUnitRepository.findAllUserByUnitForReport(unitId, search)
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