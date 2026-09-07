package id.darno.core.report

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.SimpleJasperReportsContext
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.export.SimpleExporterInput
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput
import net.sf.jasperreports.pdf.JRPdfExporter
import net.sf.jasperreports.repo.RepositoryService
import java.io.ByteArrayOutputStream

class JasperReportService {

    private val classLoader: ClassLoader =
        Thread.currentThread().contextClassLoader
            ?: JasperReportService::class.java.classLoader

    private val context: SimpleJasperReportsContext =
        createContext()

    /**
     * Generate PDF dari Collection.
     */
    suspend fun generatePdf(
        reportPath: String,
        data: Collection<*>,
        parameters: Map<String, Any> = emptyMap()
    ): ByteArray = withContext(Dispatchers.IO) {

        val dataSource = JRBeanCollectionDataSource(data)

        generatePdf(
            reportPath = reportPath,
            parameters = parameters,
            dataSource = dataSource
        )
    }

    /**
     * Generate PDF menggunakan JRDataSource.
     */
    suspend fun generatePdf(
        reportPath: String,
        parameters: Map<String, Any> = emptyMap(),
        dataSource: JRDataSource
    ): ByteArray = withContext(Dispatchers.IO) {

        val reportStream =
            classLoader.getResourceAsStream(reportPath)
                ?: throw IllegalStateException(
                    "Resource laporan tidak ditemukan di path: $reportPath"
                )

        /*
         * PENTING:
         *
         * JasperReports dapat memodifikasi parameter map
         * selama proses filling.
         *
         * Karena Map dari Kotlin bisa berupa read-only map
         * (termasuk emptyMap()), kita harus memberikan
         * mutable copy kepada JasperReports.
         */
        val jasperParameters = parameters.toMutableMap()

        reportStream.use { stream ->

            val jasperPrint =
                JasperFillManager
                    .getInstance(context)
                    .fill(
                        stream,
                        jasperParameters,
                        dataSource
                    )

            exportPdf(jasperPrint)
        }
    }

    /**
     * Membuat JasperReportsContext.
     */
    private fun createContext(): SimpleJasperReportsContext {

        val context = SimpleJasperReportsContext()

        val repositoryService =
            ClassLoaderRepositoryService(classLoader)

        context.setExtensions(
            RepositoryService::class.java,
            listOf(repositoryService)
        )

        return context
    }

    /**
     * Export JasperPrint menjadi PDF.
     */
    private fun exportPdf(
        jasperPrint: JasperPrint
    ): ByteArray {

        val exporter = JRPdfExporter(context)

        exporter.setExporterInput(
            SimpleExporterInput(jasperPrint)
        )

        val outputStream = ByteArrayOutputStream()

        exporter.setExporterOutput(
            SimpleOutputStreamExporterOutput(outputStream)
        )

        exporter.exportReport()

        return outputStream.toByteArray()
    }
}