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
import net.sf.jasperreports.repo.RepositoryService
import java.io.ByteArrayOutputStream
import java.io.StringWriter

class JasperReportService {

    private val classLoader: ClassLoader =
        Thread.currentThread().contextClassLoader
            ?: JasperReportService::class.java.classLoader

    private val context: SimpleJasperReportsContext =
        createContext()

    /**
     * Generate report menggunakan Collection sebagai datasource.
     *
     * Cocok untuk hasil query repository yang berupa:
     *
     * List<UserReportRow>
     * List<UserUnitReportRow>
     * dan sebagainya.
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
     *
     * Method ini menjadi method utama yang melakukan:
     *
     * 1. Validasi report path
     * 2. Membuka .jasper dari classpath
     * 3. Membuat parameter mutable
     * 4. Fill JasperPrint
     * 5. Export sesuai format
     * 6. Menghasilkan ReportFile
     */
    suspend fun generate(
        reportPath: String,
        format: ReportFormat,
        dataSource: JRDataSource,
        parameters: Map<String, Any> = emptyMap(),
        fileName: String? = null
    ): ReportFile = withContext(Dispatchers.IO) {

        validateReportPath(reportPath)

        val reportStream =
            classLoader.getResourceAsStream(reportPath)
                ?: throw IllegalStateException(
                    "Resource laporan tidak ditemukan di path: $reportPath"
                )

        /*
         * JasperReports dapat memodifikasi parameter map
         * ketika proses filling berlangsung.
         *
         * Jangan langsung mengirim:
         *
         *     emptyMap()
         *
         * atau:
         *
         *     mapOf(...)
         *
         * karena keduanya read-only.
         *
         * Membuat mutable copy mencegah:
         *
         * java.lang.UnsupportedOperationException:
         * Operation is not supported for read-only collection
         */
        val jasperParameters =
            parameters.toMutableMap()

        reportStream.use { stream ->

            val jasperPrint =
                JasperFillManager
                    .getInstance(context)
                    .fill(
                        stream,
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
    }

    /**
     * Membuat JasperReports context dengan RepositoryService
     * yang dapat membaca resource dari classpath.
     */
    private fun createContext(): SimpleJasperReportsContext {

        val context =
            SimpleJasperReportsContext()

        val repositoryService =
            ClassLoaderRepositoryService(
                classLoader
            )

        context.setExtensions(
            RepositoryService::class.java,
            listOf(repositoryService)
        )

        return context
    }

    /**
     * Export JasperPrint sesuai format yang diminta.
     */
    private fun export(
        jasperPrint: net.sf.jasperreports.engine.JasperPrint,
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
        jasperPrint: net.sf.jasperreports.engine.JasperPrint
    ): ByteArray {

        val exporter =
            JRPdfExporter(context)

        exporter.setExporterInput(
            SimpleExporterInput(
                jasperPrint
            )
        )

        val outputStream =
            ByteArrayOutputStream()

        exporter.setExporterOutput(
            SimpleOutputStreamExporterOutput(
                outputStream
            )
        )

        exporter.exportReport()

        return outputStream.toByteArray()
    }

    /**
     * Export XLSX.
     */
    private fun exportXlsx(
        jasperPrint: net.sf.jasperreports.engine.JasperPrint
    ): ByteArray {

        val exporter =
            JRXlsxExporter(context)

        exporter.setExporterInput(
            SimpleExporterInput(
                jasperPrint
            )
        )

        val outputStream =
            ByteArrayOutputStream()

        exporter.setExporterOutput(
            SimpleOutputStreamExporterOutput(
                outputStream
            )
        )

        val configuration =
            SimpleXlsxReportConfiguration().apply {

                /*
                 * Menghilangkan baris kosong yang tidak diperlukan.
                 */
                isRemoveEmptySpaceBetweenRows = true

                /*
                 * Tidak membuat setiap halaman Jasper
                 * menjadi worksheet terpisah.
                 */
                isOnePagePerSheet = false

                /*
                 * Membantu Jasper mendeteksi tipe data
                 * cell Excel.
                 */
                isDetectCellType = true

                /*
                 * Abaikan margin halaman Jasper
                 * ketika membuat XLSX.
                 */
                isIgnorePageMargins = true
            }

        exporter.setConfiguration(
            configuration
        )

        exporter.exportReport()

        return outputStream.toByteArray()
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
            SimpleExporterInput(
                jasperPrint
            )
        )

        val writer =
            StringWriter()

        exporter.setExporterOutput(
            SimpleWriterExporterOutput(
                writer
            )
        )

        exporter.exportReport()

        return writer
            .toString()
            .toByteArray(Charsets.UTF_8)
    }

    /**
     * Membuat nama file final.
     *
     * Contoh:
     *
     * fileName = "users"
     * format   = PDF
     *
     * hasil:
     *
     * users.pdf
     *
     * Jika fileName tidak diberikan:
     *
     * reports/users.jasper
     *
     * akan menjadi:
     *
     * users.pdf
     */
    private fun buildFileName(
        fileName: String?,
        reportPath: String,
        format: ReportFormat
    ): String {

        val baseName =
            fileName
                ?.substringBeforeLast(".")
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: reportPath
                    .substringAfterLast("/")
                    .substringBeforeLast(".")

        return "$baseName.${format.extension}"
    }

    /**
     * Validasi path report.
     */
    private fun validateReportPath(
        reportPath: String
    ) {

        require(
            reportPath.isNotBlank()
        ) {
            "reportPath tidak boleh kosong"
        }

        require(
            reportPath.endsWith(".jasper")
        ) {
            "reportPath harus menunjuk ke file .jasper: $reportPath"
        }
    }
}