package id.darno.module.wilayah.model

data class KecamatanListItem(
    val id: Short,
    val kabupatenId: Short,
    val kabupatenNama: String,
    val kode: String,
    val nama: String,
    val isActive: Boolean
)