package id.darno.module.wilayah.domain

data class KelurahanDomain(
    val id: Int,
    val kecamatanId: Short,
    val kecamatanNama: String,
    val kode: String,
    val nama: String,
    val isActive: Boolean
)