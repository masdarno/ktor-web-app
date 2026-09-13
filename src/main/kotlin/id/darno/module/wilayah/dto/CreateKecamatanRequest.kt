package id.darno.module.wilayah.dto

import org.valiktor.functions.hasSize
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

data class CreateKecamatanRequest(
    val kabupatenId: Short,
    val kode: String,
    val nama: String
) {
    init {
        validate(this) {
            validate(CreateKecamatanRequest::kabupatenId)
                .isGreaterThan(0)

            validate(CreateKecamatanRequest::kode)
                .isNotEmpty()
                .hasSize(6, 6)

            validate(CreateKecamatanRequest::nama)
                .isNotEmpty()
        }
    }
}