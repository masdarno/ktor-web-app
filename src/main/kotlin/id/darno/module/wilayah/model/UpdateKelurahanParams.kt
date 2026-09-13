package id.darno.module.wilayah.model

data class UpdateKelurahanParams(
    val kecamatanId: Short? = null,
    val kode: String? = null,
    val nama: String? = null,
    val isActive: Boolean? = null,
    val updatedBy: Short
)