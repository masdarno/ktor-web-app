package id.darno.core.storage

import id.darno.core.multipart.model.UploadedFile

interface FileStorageService {
    /**
     * Menyimpan file dan mengembalikan path/filename akhir
     */
    suspend fun save(file: UploadedFile, folder: String): String

    /**
     * Menghapus file berdasarkan nama
     */
    suspend fun delete(fileName: String, folder: String)
}