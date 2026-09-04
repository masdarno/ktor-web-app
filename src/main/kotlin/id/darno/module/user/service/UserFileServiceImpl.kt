package id.darno.module.user.service

import id.darno.core.exceptions.service.FileUploadException
import id.darno.core.model.DefaultValues
import id.darno.core.multipart.model.UploadedFile
import id.darno.core.storage.FileStorageService
import id.darno.module.user.config.UserUploadConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.util.*
import javax.imageio.ImageIO

class UserFileServiceImpl(
    private val config: UserUploadConfig,
    private val storageService: FileStorageService // Inject interface, bukan implementasi
) : UserFileService {

    private val logger = LoggerFactory.getLogger(UserFileServiceImpl::class.java)
    private val allowedExtensions = setOf("jpg", "jpeg", "png", "webp")
    private val protectedFiles = setOf(DefaultValues.DEFAULT_MALE_PHOTO, DefaultValues.DEFAULT_FEMALE_PHOTO)

    override suspend fun uploadProfilePhoto(file: UploadedFile): String {
        // --- 1. Validasi Bisnis ---
        if (file.content.isEmpty()) throw FileUploadException("File kosong.")

        if (file.contentType?.startsWith("image/") != true)
            throw FileUploadException("Tipe file tidak didukung.")

        val ext = file.name.substringAfterLast('.', "")
        if (ext !in allowedExtensions) throw FileUploadException("Format harus image.")

        if (file.content.size > config.maxPhotoSizeBytes)
            throw FileUploadException("File terlalu besar.")

        // Pindahkan operasi blocking ke Dispatchers.IO
        val isImageValid = withContext(Dispatchers.IO) {
            try {
                // ImageIO.read adalah blocking call
                val image = ImageIO.read(file.content.inputStream())
                image != null // Jika null, berarti bukan gambar yang valid
            } catch (e: Exception) {
                false
            }
        }
        if (!isImageValid) {
            throw FileUploadException("File bukan gambar valid atau rusak.")
        }

        // --- 2. Generate Nama Unik ---
        val newFileName = "${UUID.randomUUID()}.$ext"

        // Buat objek file baru dengan nama yang sudah di-rename
        val fileToSave = file.copy(name = newFileName)

        // --- 3. Delegate ke Storage Service ---
        return try {
            // Simpan ke folder "profile"
            storageService.save(fileToSave, config.profileDir)
        } catch (e: Exception) {
            logger.error("Gagal upload", e)
            throw FileUploadException("Gagal menyimpan file ke storage.")
        }
    }

    override suspend fun deleteProfilePhoto(fileName: String) {
        if (fileName in protectedFiles) return // [cite: 135]

        try {
            storageService.delete(fileName, config.profileDir)
        } catch (e: Exception) {
            logger.error("Gagal hapus file: $fileName", e)
        }
    }
}