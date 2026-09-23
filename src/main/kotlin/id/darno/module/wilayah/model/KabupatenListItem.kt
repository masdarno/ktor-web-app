package id.darno.module.wilayah.model

data class KabupatenListItem(
    val id: Short,
    val provinsiId: Short,
    val provinsiNama: String,
    val kode: String,
    val nama: String,
    val isActive: Boolean
)