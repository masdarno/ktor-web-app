package id.darno.module.wilayah.dto

data class CreateKelurahanRequest(
    val kecamatanId: Short,
    val kode: String,
    val nama: String
)