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
import net.sf.jasperreports.engine.util.JRResourcesUtil
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

    /*
     * PENTING:
     * Classloader ini adalah FALLBACK saja, dipakai untuk resolve
     * resource seperti direktori "reports" di classpath.
     *
     * JANGAN dianggap sebagai classloader yang sama dengan yang
     * memuat DTO aplikasi. Kalau JasperReportService ini berada di
     * module/library yang terpisah dari module tempat DTO
     * didefinisikan (mis. "core" vs "app"), classloader-nya BISA
     * berbeda dari classloader yang dipakai Ktor untuk memuat DTO
     * (Ktor memakai OverridingClassLoader saat development/run).
     *
     * Classloader yang BENAR-BENAR dipakai untuk fill report harus
     * diambil dari objek data/DTO itu sendiri saat runtime, lihat
     * resolveClassLoader().
     */
    private val classLoader: ClassLoader =
        JasperReportService::class.java.classLoader

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
        val dataSource = JRBeanCollectionDataSource(data)

        /*
         * CLASSLOADER:
         * Cari classloader dari kandidat objek aplikasi yang ada
         * (baik dari data maupun parameters), bukan cuma dari
         * elemen pertama "data". Kalau "data" kosong (0 baris),
         * mengandalkan data.firstOrNull() saja akan salah jatuh ke
         * fallback classLoader milik JasperReportService, padahal
         * DTO lain (mis. KOP_SURAT) mungkin tetap dikirim lewat
         * parameters dan perlu classloader yang sama dengannya.
         */
        val effectiveClassLoader =
            resolveClassLoader(data, parameters)

        return generate(
            reportPath = reportPath,
            format = format,
            dataSource = dataSource,
            parameters = parameters,
            fileName = fileName,
            effectiveClassLoader = effectiveClassLoader
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
        fileName: String? = null,
        effectiveClassLoader: ClassLoader = classLoader
    ): ReportFile = withContext(Dispatchers.IO) {

        validateReportPath(reportPath)

        val jasperParameters = parameters.toMutableMap()

        /*
         * CLASSLOADER:
         * Beritahu JasperReports classloader yang benar (classloader
         * DTO aplikasi), bukan classloader JasperReportService.
         */
        jasperParameters["REPORT_CLASS_LOADER"] = effectiveClassLoader

        val repositoryReportPath =
            resolveReportPath(reportPath)

        val originalContextClassLoader =
            Thread.currentThread().contextClassLoader

        val jasperPrint =
            try {
                /*
                 * CLASSLOADER (1/2):
                 * Set classloader JasperReports internal (dipakai
                 * saat evaluasi expression, dsb).
                 */
                JRResourcesUtil.setThreadClassLoader(effectiveClassLoader)

                /*
                 * CLASSLOADER (2/2):
                 * Set juga context classloader Java standar, karena
                 * deserialisasi objek JasperReport dari file .jasper
                 * (lewat ObjectInputStream di JRLoader) memakai
                 * Thread.currentThread().contextClassLoader, BUKAN
                 * ThreadLocal milik JRResourcesUtil.
                 *
                 * Selalu dikembalikan di blok finally supaya tidak
                 * "bocor" ke coroutine/thread lain setelah selesai.
                 */
                Thread.currentThread().contextClassLoader =
                    effectiveClassLoader

                JasperFillManager
                    .getInstance(context)
                    .fillFromRepo(
                        repositoryReportPath,
                        jasperParameters,
                        dataSource
                    )

            } finally {
                /*
                 * Kembalikan ThreadLocal classloader JasperReports
                 * dan context classloader Java setelah proses fill
                 * selesai.
                 */
                JRResourcesUtil.resetClassLoader()
                Thread.currentThread().contextClassLoader =
                    originalContextClassLoader
            }

        val content =
            export(
                jasperPrint,
                format
            )

        ReportFile(
            content = content,
            fileName = buildFileName(
                fileName,
                reportPath,
                format
            ),
            contentType = format.contentType
        )
    }

    /*
     * Cari classloader dari objek data aplikasi yang benar-benar
     * ada di request ini, dengan urutan prioritas:
     *
     *   1. Elemen pertama dari "data" (baris detail report).
     *   2. Nilai pertama di "parameters" yang bukan tipe milik
     *      JasperReports/JDK sendiri (mis. KOP_SURAT dto, dsb).
     *   3. Fallback ke classLoader milik JasperReportService kalau
     *      tidak ada kandidat sama sekali (mis. data & parameters
     *      keduanya kosong/tidak berisi DTO aplikasi).
     *
     * Ini penting terutama saat "data" kosong (0 baris) tapi
     * "parameters" tetap membawa DTO aplikasi lain (mis. dto untuk
     * kop surat/header) yang perlu classloader yang sama.
     *
     * PERHATIAN proxy: kalau objek yang ditemukan adalah proxy
     * (mis. CGLIB proxy dari Spring @Transactional), classloadernya
     * bisa jadi classloader proxy, bukan classloader DTO asli.
     * Kalau ini terjadi, sebaiknya mapping ke DTO murni (data class
     * biasa) sebelum dikirim ke generate(), bukan meneruskan
     * entity/proxy langsung.
     */
    private fun resolveClassLoader(
        data: Collection<*>,
        parameters: Map<String, Any>
    ): ClassLoader {

        val fromData = data.firstOrNull()

        val fromParameters =
            parameters.values.firstOrNull { value ->
                !isFrameworkType(value)
            }

        return (fromData ?: fromParameters)
            ?.javaClass
            ?.classLoader
            ?: classLoader
    }

    /*
     * Tipe "milik framework" (Jasper, JDK dasar) tidak relevan
     * untuk deteksi classloader aplikasi, karena selalu dimuat
     * lewat bootstrap/platform classloader, bukan classloader
     * yang memuat DTO aplikasi.
     */
    private fun isFrameworkType(value: Any?): Boolean {

        if (value == null) return true

        val packageName =
            value.javaClass.`package`?.name.orEmpty()

        return packageName.startsWith("java.") ||
                packageName.startsWith("javax.") ||
                packageName.startsWith("kotlin.") ||
                packageName.startsWith("net.sf.jasperreports.")
    }

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

        /*
         * JANGAN tambahkan:
         *
         * repositoryService.setClassLoader(classLoader)
         *
         * FileRepositoryService pada JasperReports 7.0.8
         * tidak memiliki method tersebut.
         */

        context.setExtensions(
            RepositoryService::class.java,
            listOf(repositoryService)
        )

        return context
    }

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
    ): ByteArray =
        when (format) {
            ReportFormat.PDF ->
                exportPdf(jasperPrint)

            ReportFormat.XLSX ->
                exportXlsx(jasperPrint)

            ReportFormat.CSV ->
                exportCsv(jasperPrint)
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