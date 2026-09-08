package id.darno.core.report

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.SimpleJasperReportsContext
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.engine.export.JRCsvExporter
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
import net.sf.jasperreports.export.SimpleExporterInput
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput
import net.sf.jasperreports.export.SimpleWriterExporterOutput
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration
import net.sf.jasperreports.pdf.JRPdfExporter
import net.sf.jasperreports.repo.FileRepositoryService
import net.sf.jasperreports.repo.RepositoryService
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.StringWriter

class JasperReportService {

    companion object {
        private const val REPORT_ROOT = "reports"
    }

    private val classLoader: ClassLoader =
        Thread.currentThread().contextClassLoader
            ?: JasperReportService::class.java.classLoader

    private val context: SimpleJasperReportsContext =
        createContext()

    /**
     * Generate report dari Collection.
     */
    suspend fun generate(
        reportPath: String,
        format: ReportFormat,
        data: Collection<*>,
        parameters: Map<String, Any> = emptyMap(),
        fileName: String? = null
    ): ReportFile {

        val dataSource =
            JRBeanCollectionDataSource(data)

        return generate(
            reportPath = reportPath,
            format = format,
            dataSource = dataSource,
            parameters = parameters,
            fileName = fileName
        )
    }

    /**
     * Generate report menggunakan JRDataSource.
     */
    suspend fun generate(
        reportPath: String,
        format: ReportFormat,
        dataSource: JRDataSource,
        parameters: Map<String, Any> = emptyMap(),
        fileName: String? = null
    ): ReportFile = withContext(Dispatchers.IO) {

        validateReportPath(reportPath)

        /*
         * JasperReports dapat memodifikasi parameter selama proses fill.
         * Karena itu gunakan MutableMap.
         */
        val jasperParameters =
            parameters.toMutableMap()

        /*
         * Contoh:
         *
         * users.jasper
         *     ->
         * reports/users.jasper
         *
         * shared/users.jasper
         *     ->
         * reports/shared/users.jasper
         */
        val repositoryReportPath =
            resolveReportPath(reportPath)

        /*
         * Gunakan fillFromRepo(), bukan fill(InputStream,...).
         *
         * Ini penting karena Jasper harus mengetahui lokasi report
         * agar resource relatif seperti:
         *
         * shared/MY_STYLES.jrtx
         * shared/KopSurat.jasper
         * logo-pemkab-boyolali.jpg
         *
         * dapat di-resolve dengan benar.
         */
        val jasperPrint =
            JasperFillManager
                .getInstance(context)
                .fillFromRepo(
                    repositoryReportPath,
                    jasperParameters,
                    dataSource
                )

        val content =
            export(
                jasperPrint = jasperPrint,
                format = format
            )

        ReportFile(
            content = content,
            fileName = buildFileName(
                fileName = fileName,
                reportPath = reportPath,
                format = format
            ),
            contentType = format.contentType
        )
    }

    /**
     * Membuat JasperReports context.
     *
     * Repository root sengaja diarahkan ke:
     *
     * build/resources/main
     *
     * bukan langsung ke:
     *
     * build/resources/main/reports
     *
     * Karena report path yang digunakan Jasper adalah:
     *
     * reports/users.jasper
     *
     * sehingga semua resource berada dalam satu root:
     *
     * build/resources/main/
     * └── reports/
     */
    private fun createContext(): SimpleJasperReportsContext {

        val repositoryRoot =
            findRepositoryRoot()

        val context =
            SimpleJasperReportsContext()

        val repositoryService =
            FileRepositoryService(
                context,
                repositoryRoot,
                true
            )

        context.setExtensions(
            RepositoryService::class.java,
            listOf(repositoryService)
        )

        return context
    }

    /**
     * Mendapatkan root repository.
     *
     * Jika:
     *
     * classLoader.getResource("reports")
     *
     * menghasilkan:
     *
     * file:/.../build/resources/main/reports
     *
     * maka repository root yang digunakan adalah:
     *
     * /.../build/resources/main
     */
    private fun findRepositoryRoot(): String {

        val reportsUrl =
            classLoader.getResource(REPORT_ROOT)
                ?: throw IllegalStateException(
                    "Resource '$REPORT_ROOT' tidak ditemukan di classpath."
                )

        require(reportsUrl.protocol == "file") {
            "Resource '$REPORT_ROOT' harus berupa directory " +
                    "filesystem. Protocol yang ditemukan: " +
                    reportsUrl.protocol
        }

        val reportsDirectory =
            File(reportsUrl.toURI())

        require(reportsDirectory.isDirectory) {
            "Directory '$REPORT_ROOT' tidak ditemukan: " +
                    reportsDirectory.absolutePath
        }

        val repositoryRoot =
            reportsDirectory.parentFile
                ?: throw IllegalStateException(
                    "Parent directory dari reports tidak ditemukan: " +
                            reportsDirectory.absolutePath
                )

        return repositoryRoot.absolutePath
    }

    /**
     * Mengubah report path menjadi repository path JasperReports.
     *
     * users.jasper
     *     -> reports/users.jasper
     *
     * shared/users.jasper
     *     -> reports/shared/users.jasper
     *
     * reports/users.jasper
     *     -> reports/users.jasper
     */
    private fun resolveReportPath(
        reportPath: String
    ): String {

        val normalized =
            reportPath
                .removePrefix("/")
                .replace("\\", "/")

        return if (
            normalized == REPORT_ROOT ||
            normalized.startsWith("$REPORT_ROOT/")
        ) {
            normalized
        } else {
            "$REPORT_ROOT/$normalized"
        }
    }

    /**
     * Validasi report path.
     */
    private fun validateReportPath(
        reportPath: String
    ) {

        val normalized =
            reportPath
                .replace("\\", "/")
                .removePrefix("/")

        require(normalized.isNotBlank()) {
            "reportPath tidak boleh kosong."
        }

        require(!normalized.contains("..")) {
            "reportPath tidak boleh mengandung '..': $reportPath"
        }

        require(
            normalized.endsWith(
                ".jasper",
                ignoreCase = true
            )
        ) {
            "reportPath harus berupa file .jasper: $reportPath"
        }
    }

    /**
     * Export JasperPrint sesuai format.
     */
    private fun export(
        jasperPrint: JasperPrint,
        format: ReportFormat
    ): ByteArray {

        return when (format) {

            ReportFormat.PDF ->
                exportPdf(jasperPrint)

            ReportFormat.XLSX ->
                exportXlsx(jasperPrint)

            ReportFormat.CSV ->
                exportCsv(jasperPrint)
        }
    }

    /**
     * Export PDF.
     */
    private fun exportPdf(
        jasperPrint: JasperPrint
    ): ByteArray {

        val output =
            ByteArrayOutputStream()

        val exporter =
            JRPdfExporter(context)

        exporter.setExporterInput(
            SimpleExporterInput(jasperPrint)
        )

        exporter.setExporterOutput(
            SimpleOutputStreamExporterOutput(output)
        )

        exporter.exportReport()

        return output.toByteArray()
    }

    /**
     * Export XLSX.
     */
    private fun exportXlsx(
        jasperPrint: JasperPrint
    ): ByteArray {

        val output =
            ByteArrayOutputStream()

        val exporter =
            JRXlsxExporter(context)

        exporter.setExporterInput(
            SimpleExporterInput(jasperPrint)
        )

        exporter.setExporterOutput(
            SimpleOutputStreamExporterOutput(output)
        )

        val configuration =
            SimpleXlsxReportConfiguration().apply {

                isOnePagePerSheet = false
                isDetectCellType = true
                isRemoveEmptySpaceBetweenRows = true
                isRemoveEmptySpaceBetweenColumns = true
            }

        exporter.setConfiguration(
            configuration
        )

        exporter.exportReport()

        return output.toByteArray()
    }

    /**
     * Export CSV.
     */
    private fun exportCsv(
        jasperPrint: JasperPrint
    ): ByteArray {

        val exporter =
            JRCsvExporter(context)

        exporter.setExporterInput(
            SimpleExporterInput(jasperPrint)
        )

        val writer =
            StringWriter()

        exporter.setExporterOutput(
            SimpleWriterExporterOutput(writer)
        )

        exporter.exportReport()

        return writer
            .toString()
            .toByteArray(Charsets.UTF_8)
    }

    /**
     * Membuat nama file output.
     */
    private fun buildFileName(
        fileName: String?,
        reportPath: String,
        format: ReportFormat
    ): String {

        val baseName =
            fileName
                ?.substringBeforeLast(".")
                ?.takeIf { it.isNotBlank() }
                ?: reportPath
                    .substringAfterLast("/")
                    .substringBeforeLast(".")

        return "$baseName.${format.extension}"
    }
}