package id.darno.module.wilayah.dto

data class UpdateKelurahanRequest (
    val kecamatanId: Short,
    val kode: String,
    val nama: String,
    val isActive: Boolean
)