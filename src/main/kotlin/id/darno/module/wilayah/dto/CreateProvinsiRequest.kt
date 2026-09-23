package id.darno.module.wilayah.dto

data class CreateProvinsiRequest(
    val kode: String,
    val nama: String,
    val isActive: Boolean
)