package id.darno.module.wilayah.model

data class KelurahanListItem(
    val id: Int,
    val kecamatanId: Short,
    val kecamatanNama: String,
    val kode: String,
    val nama: String,
    val isActive: Boolean
)