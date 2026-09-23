package id.darno.module.wilayah.mapper

import id.darno.module.wilayah.dto.UpdateKelurahanRequest
import id.darno.module.wilayah.model.UpdateKelurahanParams

fun UpdateKelurahanRequest.toUpdateKelurahanParams(
    updatedBy: Short
) = UpdateKelurahanParams(
    kecamatanId = kecamatanId,
    kode = kode,
    nama = nama,
    isActive = isActive,
    updatedBy = updatedBy
)