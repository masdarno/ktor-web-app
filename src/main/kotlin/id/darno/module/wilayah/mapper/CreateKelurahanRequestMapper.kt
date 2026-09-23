package id.darno.module.wilayah.mapper

import id.darno.module.wilayah.dto.CreateKelurahanRequest
import id.darno.module.wilayah.model.CreateKelurahanParams

fun CreateKelurahanRequest.toCreateKelurahanParams(
    createdBy: Short
) = CreateKelurahanParams(
    kecamatanId = kecamatanId,
    kode = kode,
    nama = nama,
    createdBy = createdBy
)