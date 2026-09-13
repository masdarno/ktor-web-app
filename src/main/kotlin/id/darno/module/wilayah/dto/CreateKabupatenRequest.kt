package id.darno.module.wilayah.dto

import org.valiktor.functions.hasSize
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

data class CreateKabupatenRequest(
    val provinsiId: Short,
    val kode: String,
    val nama: String
) {
    init {
        validate(this) {
            validate(CreateKabupatenRequest::provinsiId)
                .isGreaterThan(0)

            validate(CreateKabupatenRequest::kode)
                .isNotEmpty()
                .hasSize(4, 4)

            validate(CreateKabupatenRequest::nama)
                .isNotEmpty()
        }
    }
}