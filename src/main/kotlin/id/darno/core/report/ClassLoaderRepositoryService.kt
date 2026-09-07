package id.darno.core.report

import net.sf.jasperreports.repo.InputStreamResource
import net.sf.jasperreports.repo.RepositoryService
import net.sf.jasperreports.repo.Resource

class ClassLoaderRepositoryService(
    private val classLoader: ClassLoader
) : RepositoryService {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Resource?> getResource(
        location: String?,
        javaType: Class<T>?
    ): T {

        val resource = getResource(location)

        if (
            resource != null &&
            (javaType == null || javaType.isInstance(resource))
        ) {
            return resource as T
        }

        return null as T
    }

    override fun getResource(
        location: String?
    ): Resource? {

        if (location.isNullOrEmpty()) {
            return null
        }

        /*
         * JasperReports dapat meminta resource seperti:
         *
         * shared/header.jasper
         * shared/footer.jasper
         *
         * Sedangkan resource sebenarnya berada di:
         *
         * src/main/resources/reports/shared/header.jasper
         * src/main/resources/reports/shared/footer.jasper
         *
         * Maka prefix "shared/" diarahkan ke "reports/shared/".
         */
        val resourcePath =
            if (location.startsWith("shared/")) {
                "reports/$location"
            } else {
                location
            }

        val stream =
            classLoader.getResourceAsStream(resourcePath)
                ?: return null

        return InputStreamResource().apply {
            inputStream = stream
        }
    }

    override fun saveResource(
        location: String?,
        resource: Resource?
    ) {
        /*
         * Repository ini hanya digunakan untuk membaca resource
         * dari classpath. Tidak ada operasi penyimpanan.
         */
    }
}