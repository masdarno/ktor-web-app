package id.darno.module.wilayah.model

data class CreateKabupatenParams(
    val provinsiId: Short,
    val kode: String,
    val nama: String,
    val isActive: Boolean = true,
    val createdBy: Short
)