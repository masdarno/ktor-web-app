package id.darno.core.multipart.mapper

import id.darno.core.multipart.model.MultiPartContent
import id.darno.core.multipart.model.UploadedFile
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray

suspend fun MultiPartData.extractContent(): MultiPartContent {
    val formItems = mutableMapOf<String, MutableList<String>>()
    val fileItems = mutableListOf<UploadedFile>()

    // ... (Logika forEachPart untuk mengisi formItems dan fileItems) ...
    forEachPart { part ->
        val partName = part.name ?: return@forEachPart

        when (part) {
            is PartData.FormItem -> {
                formItems.getOrPut(partName) { mutableListOf() }.add(part.value)
            }
            is PartData.FileItem -> {
                val fileName = part.originalFileName ?: "unknown"
                val contentType = part.contentType?.toString()
                val fileBytes =  part.provider().readRemaining().readByteArray()
                fileItems.add(
                    UploadedFile(
                        name = fileName,
                        partName = partName,
                        contentType = contentType,
                        content = fileBytes
                    )
                )
            }
            else -> {}
        }
        part.dispose()
    }

    // 1. Konversi ke Map<String, List<String>> biasa
    val mapForParameters = formItems.mapValues { it.value.toList() }

    // ⭐ SOLUSI ANTI-EROR (Menggunakan Builder Ktor) ⭐
    // 2. Bangun objek Parameters secara eksplisit menggunakan builder.
    val finalParameters: Parameters = Parameters.build {
        mapForParameters.forEach { (key, values) ->
            // appendAll akan menambahkan semua nilai List<String> ke kunci yang sama
            appendAll(key, values)
        }
    }

    return MultiPartContent(
        form = finalParameters, // Sekarang ini adalah objek Parameters yang sah
        files = fileItems.toList()
    )
}