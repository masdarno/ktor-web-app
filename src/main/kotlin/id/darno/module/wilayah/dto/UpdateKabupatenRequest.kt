package id.darno.module.wilayah.dto

data class UpdateKabupatenRequest (
    val provinsiId: Short,
    val kode: String,
    val nama: String,
    val isActive: Boolean
)