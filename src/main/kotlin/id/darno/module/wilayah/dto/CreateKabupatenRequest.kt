package id.darno.module.wilayah.dto

data class CreateKabupatenRequest(
    val provinsiId: Short,
    val kode: String,
    val nama: String
)