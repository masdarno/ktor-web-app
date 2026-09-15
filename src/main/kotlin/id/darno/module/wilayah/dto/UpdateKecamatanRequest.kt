package id.darno.module.wilayah.dto

data class UpdateKecamatanRequest (
    val kabupatenId: Short,
    val kode: String,
    val nama: String,
    val isActive: Boolean
)