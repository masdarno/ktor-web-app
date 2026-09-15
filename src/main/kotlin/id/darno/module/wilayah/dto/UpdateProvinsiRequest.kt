package id.darno.module.wilayah.dto

import org.valiktor.functions.hasSize
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

data class UpdateProvinsiRequest(
    val kode: String,
    val nama: String,
    val isActive: Boolean
)