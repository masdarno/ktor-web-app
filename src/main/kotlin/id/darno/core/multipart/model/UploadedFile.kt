package id.darno.core.multipart.model

data class UploadedFile(
    val name: String,
    val partName: String,
    val contentType: String?,
    val content: ByteArray
) {
    // Harus mengOverride equals() untuk membandingkan isi array
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UploadedFile

        if (name != other.name) return false
        if (partName != other.partName) return false
        if (contentType != other.contentType) return false
        if (!content.contentEquals(other.content)) return false // ⭐ PENTING: Gunakan contentEquals()

        return true
    }

    // Harus mengOverride hashCode() menggunakan array.contentHashCode()
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + partName.hashCode()
        result = 31 * result + (contentType?.hashCode() ?: 0)
        result = 31 * result + content.contentHashCode() // ⭐ PENTING: Gunakan contentHashCode()
        return result
    }
}