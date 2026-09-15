package id.darno.module.wilayah.dto

import org.valiktor.functions.hasSize
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

data class CreateKabupatenRequest(
    val provinsiId: Short,
    val kode: String,
    val nama: String
)