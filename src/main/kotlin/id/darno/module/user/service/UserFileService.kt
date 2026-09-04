package id.darno.module.user.service

import id.darno.core.multipart.model.UploadedFile

interface UserFileService {
    suspend fun uploadProfilePhoto(file: UploadedFile): String
    suspend fun deleteProfilePhoto(fileName: String)
}
