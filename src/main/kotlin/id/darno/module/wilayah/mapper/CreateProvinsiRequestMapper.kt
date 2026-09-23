package id.darno.module.wilayah.mapper

import id.darno.module.wilayah.dto.CreateProvinsiRequest
import id.darno.module.wilayah.model.CreateProvinsiParams

fun CreateProvinsiRequest.toCreateProvinsiParams(
    createdBy: Short
) = CreateProvinsiParams(
    kode = kode,
    nama = nama,
    isActive = isActive,
    createdBy = createdBy
)