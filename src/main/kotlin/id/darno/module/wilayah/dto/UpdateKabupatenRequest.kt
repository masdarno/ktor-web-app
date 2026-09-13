package id.darno.module.wilayah.dto

import org.valiktor.functions.hasSize
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

data class UpdateKabupatenRequest (
    val provinsiId: Short,
    val kode: String,
    val nama: String,
    val isActive: Boolean
) {
    init {
        validate(this) {
            validate(UpdateKabupatenRequest::provinsiId)
                .isGreaterThan(0)

            validate(UpdateKabupatenRequest::kode)
                .isNotEmpty()
                .hasSize(4, 4)

            validate(UpdateKabupatenRequest::nama)
                .isNotEmpty()
        }
    }
}