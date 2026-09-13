package id.darno.module.wilayah.dto

import org.valiktor.functions.hasSize
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

data class CreateKelurahanRequest(
    val kecamatanId: Short,
    val kode: String,
    val nama: String
) {
    init {
        validate(this) {
            validate(CreateKelurahanRequest::kecamatanId)
                .isGreaterThan(0)

            validate(CreateKelurahanRequest::kode)
                .isNotEmpty()
                .hasSize(10, 10)

            validate(CreateKelurahanRequest::nama)
                .isNotEmpty()
        }
    }
}