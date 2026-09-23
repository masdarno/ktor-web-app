package id.darno.module.wilayah.model

data class CreateKelurahanParams(
    val kecamatanId: Short,
    val kode: String,
    val nama: String,
    val isActive: Boolean = true,
    val createdBy: Short
)