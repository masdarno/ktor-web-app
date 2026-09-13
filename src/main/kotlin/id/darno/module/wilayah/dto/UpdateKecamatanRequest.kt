package id.darno.module.wilayah.dto

import org.valiktor.functions.hasSize
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

data class UpdateKecamatanRequest (
    val kabupatenId: Short,
    val kode: String,
    val nama: String,
    val isActive: Boolean
) {
    init {
        validate(this) {
            validate(UpdateKecamatanRequest::kabupatenId)
                .isGreaterThan(0)

            validate(UpdateKecamatanRequest::kode)
                .isNotEmpty()
                .hasSize(6, 6)

            validate(UpdateKecamatanRequest::nama)
                .isNotEmpty()
        }
    }
}