package id.darno.core.report

import net.sf.jasperreports.repo.InputStreamResource
import net.sf.jasperreports.repo.RepositoryService
import net.sf.jasperreports.repo.Resource

/**
 * RepositoryService untuk membaca resource JasperReports
 * dari classpath.
 *
 * Contoh:
 *   shared/MY_STYLES.jrtx
 *
 * akan dicari sebagai:
 *   reports/shared/MY_STYLES.jrtx
 */
class ClassLoaderRepositoryService(
    private val classLoader: ClassLoader
) : RepositoryService {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Resource?> getResource(
        location: String?,
        resourceType: Class<T>?
    ): T {

        val resource = getResource(location)

        if (
            resource != null &&
            (resourceType == null || resourceType.isInstance(resource))
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
         * Jasper biasanya memberikan path relatif seperti:
         *
         * shared/MY_STYLES.jrtx
         *
         * Sedangkan resource aplikasi berada di:
         *
         * resources/reports/shared/MY_STYLES.jrtx
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
         * Repository ini read-only.
         *
         * Resource JasperReports hanya dibaca dari classpath.
         */
    }
}