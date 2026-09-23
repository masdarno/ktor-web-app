package id.darno.module.wilayah.domain

data class KabupatenDomain(
    val id: Short,
    val provinsiId: Short,
    val provinsiNama: String,
    val kode: String,
    val nama: String,
    val isActive: Boolean
)