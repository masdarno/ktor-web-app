package id.darno.module.wilayah.dto

data class UpdateProvinsiRequest(
    val kode: String,
    val nama: String,
    val isActive: Boolean
)