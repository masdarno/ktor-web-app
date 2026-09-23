package id.darno.core.storage.local

import id.darno.core.multipart.model.UploadedFile
import id.darno.core.storage.FileStorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalFileStorageService(
    private val baseDir: File
) : FileStorageService {

    init {
        // Pastikan direktori dasar ada saat inisialisasi
        if (!baseDir.exists()) baseDir.mkdirs()
    }

    override suspend fun save(file: UploadedFile, folder: String): String = withContext(Dispatchers.IO) {
        // Buat sub-folder (misal: "profile")
        val targetDir = File(baseDir, folder)
        if (!targetDir.exists()) targetDir.mkdirs()

        // Gabungkan folder + nama file
        val targetFile = File(targetDir, file.name)

        // Tulis bytes (Logika dari kode lama Anda)
        targetFile.writeBytes(file.content)

        // Return nama file saja (atau relative path sesuai kebutuhan database)
        file.name
    }

    override suspend fun delete(fileName: String, folder: String) = withContext(Dispatchers.IO) {
        val targetFile = File(File(baseDir, folder), fileName)
        if (targetFile.exists()) {
            targetFile.delete()
        }
        Unit
    }
}