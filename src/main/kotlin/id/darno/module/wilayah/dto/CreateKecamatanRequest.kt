package id.darno.module.wilayah.dto

data class CreateKecamatanRequest(
    val kabupatenId: Short,
    val kode: String,
    val nama: String
)