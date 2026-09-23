package id.darno.module.wilayah.model

data class CreateProvinsiParams(
    val kode: String,
    val nama: String,
    val isActive: Boolean = true,
    val createdBy: Short
)