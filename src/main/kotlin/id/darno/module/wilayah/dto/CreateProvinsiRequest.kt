package id.darno.module.wilayah.dto

import org.valiktor.functions.hasSize
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

data class CreateProvinsiRequest(
    val kode: String,
    val nama: String,
    val isActive: Boolean
) {
    init {
        validate(this) {
            validate(CreateProvinsiRequest::kode)
                .isNotEmpty()
                .hasSize(2, 2)

            validate(CreateProvinsiRequest::nama)
                .isNotEmpty()
        }
    }
}