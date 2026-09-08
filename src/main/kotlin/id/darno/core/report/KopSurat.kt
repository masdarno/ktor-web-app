package id.darno.core.report

data class KopSurat(
    val namaPemerintah: String,
    val namaPerusahaan: String,
    val namaSingkat: String,
    val alamat: String,
    val logoKanan: String,
    val logoKiri: String
)

object KopSuratParameters {

    fun from(kopSurat: KopSurat): Map<String, Any> = mapOf(
        "KOP_SURAT_NAMA_PEMERINTAH" to kopSurat.namaPemerintah,
        "KOP_SURAT_NAMA_PERUSAHAAN" to kopSurat.namaPerusahaan,
        "KOP_SURAT_NAMA_SINGKAT" to kopSurat.namaSingkat,
        "KOP_SURAT_ALAMAT" to kopSurat.alamat,
        "KOP_SURAT_LOGO_KANAN" to kopSurat.logoKanan,
        "KOP_SURAT_LOGO_KIRI" to kopSurat.logoKiri
    )
}