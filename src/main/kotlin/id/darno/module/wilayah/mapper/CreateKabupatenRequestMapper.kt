package id.darno.module.wilayah.mapper

import id.darno.module.wilayah.dto.CreateKabupatenRequest
import id.darno.module.wilayah.model.CreateKabupatenParams

fun CreateKabupatenRequest.toCreateKabupatenParams(
    createdBy: Short
) = CreateKabupatenParams(
    provinsiId = provinsiId,
    kode = kode,
    nama = nama,
    createdBy = createdBy
)