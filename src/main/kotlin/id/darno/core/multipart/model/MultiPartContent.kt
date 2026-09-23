package id.darno.core.multipart.model

import io.ktor.http.*

data class MultiPartContent(
    // Ubah tipe field ini menjadi Parameters
    val form: Parameters,
    val files: List<UploadedFile>
)