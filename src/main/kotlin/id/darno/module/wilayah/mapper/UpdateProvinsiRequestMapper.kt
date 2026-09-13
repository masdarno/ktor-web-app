package id.darno.module.wilayah.mapper

import id.darno.module.wilayah.dto.UpdateProvinsiRequest
import id.darno.module.wilayah.model.UpdateProvinsiParams

fun UpdateProvinsiRequest.toUpdateProvinsiParams(
    updatedBy: Short
) = UpdateProvinsiParams(
    kode = kode,
    nama = nama,
    isActive = isActive,
    updatedBy = updatedBy
)