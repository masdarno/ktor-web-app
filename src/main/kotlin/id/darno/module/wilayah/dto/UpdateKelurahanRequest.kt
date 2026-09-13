package id.darno.module.wilayah.dto

import org.valiktor.functions.hasSize
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

data class UpdateKelurahanRequest (
    val kecamatanId: Short,
    val kode: String,
    val nama: String,
    val isActive: Boolean
) {
    init {
        validate(this) {
            validate(UpdateKelurahanRequest::kecamatanId)
                .isGreaterThan(0)

            validate(UpdateKelurahanRequest::kode)
                .isNotEmpty()
                .hasSize(10, 10)

            validate(UpdateKelurahanRequest::nama)
                .isNotEmpty()
        }
    }
}