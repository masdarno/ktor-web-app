package id.darno.module.wilayah.model

data class CreateKecamatanParams(
    val kabupatenId: Short,
    val kode: String,
    val nama: String,
    val isActive: Boolean = true,
    val createdBy: Short
)