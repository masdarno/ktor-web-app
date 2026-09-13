package id.darno.module.wilayah.mapper

import id.darno.module.wilayah.dto.UpdateKabupatenRequest
import id.darno.module.wilayah.model.UpdateKabupatenParams

fun UpdateKabupatenRequest.toUpdateKabupatenParams(
    updatedBy: Short
) = UpdateKabupatenParams(
    provinsiId = provinsiId,
    kode = kode,
    nama = nama,
    isActive = isActive,
    updatedBy = updatedBy
)