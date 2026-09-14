package id.darno.core.validation.rules

import id.darno.core.multipart.model.UploadedFile
import id.darno.core.validation.FieldValidator
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

fun FieldValidator.requiredFile(
    value: UploadedFile?,
    field: String,
    message: String = "file harus dipilih"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it != null && it.content.isNotEmpty()
    }
}

fun FieldValidator.maxFileSize(
    value: UploadedFile,
    field: String,
    maxBytes: Long,
    message: String = "ukuran file terlalu besar"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        it.content.size.toLong() <= maxBytes
    }
}

fun FieldValidator.allowedExtensions(
    value: UploadedFile,
    field: String,
    extensions: Set<String>,
    message: String = "format file tidak diizinkan"
) {
    val allowed = extensions.map {
        it.removePrefix(".").lowercase()
    }.toSet()

    check(
        value = value,
        field = field,
        message = message
    ) {
        val extension = value.name
            .substringAfterLast('.', "")
            .lowercase()

        extension.isNotEmpty() && extension in allowed
    }
}

fun FieldValidator.allowedMimeTypes(
    value: UploadedFile,
    field: String,
    mimeTypes: Set<String>,
    message: String = "tipe file tidak diizinkan"
) {
    val allowed = mimeTypes
        .map { it.lowercase() }
        .toSet()

    check(
        value = value,
        field = field,
        message = message
    ) {
        value.contentType
            ?.lowercase()
            ?.let { it in allowed }
            ?: false
    }
}

fun FieldValidator.validImage(
    value: UploadedFile,
    field: String,
    message: String = "file bukan gambar yang valid"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        ImageIO.read(
            ByteArrayInputStream(value.content)
        ) != null
    }
}

fun FieldValidator.imageDimensions(
    value: UploadedFile,
    field: String,
    minWidth: Int? = null,
    maxWidth: Int? = null,
    minHeight: Int? = null,
    maxHeight: Int? = null,
    message: String = "dimensi gambar tidak valid"
) {
    check(
        value = value,
        field = field,
        message = message
    ) {
        val image = ImageIO.read(
            ByteArrayInputStream(value.content)
        ) ?: return@check false

        val widthValid =
            minWidth == null || image.width >= minWidth

        val maxWidthValid =
            maxWidth == null || image.width <= maxWidth

        val heightValid =
            minHeight == null || image.height >= minHeight

        val maxHeightValid =
            maxHeight == null || image.height <= maxHeight

        widthValid &&
                maxWidthValid &&
                heightValid &&
                maxHeightValid
    }
}