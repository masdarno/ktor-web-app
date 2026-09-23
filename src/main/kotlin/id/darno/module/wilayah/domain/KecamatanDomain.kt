package id.darno.module.wilayah.domain

data class KecamatanDomain(
    val id: Short,
    val kabupatenId: Short,
    val kabupatenNama: String,
    val kode: String,
    val nama: String,
    val isActive: Boolean
)