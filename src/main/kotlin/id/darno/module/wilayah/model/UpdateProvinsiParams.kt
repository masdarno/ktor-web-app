package id.darno.module.wilayah.model

data class UpdateProvinsiParams(
    val kode: String? = null,
    val nama: String? = null,
    val isActive: Boolean? = null,
    val updatedBy: Short
)