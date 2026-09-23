package id.darno.module.wilayah.mapper

import id.darno.module.wilayah.dto.CreateKecamatanRequest
import id.darno.module.wilayah.model.CreateKecamatanParams

fun CreateKecamatanRequest.toCreateKecamatanParams(
    createdBy: Short
) = CreateKecamatanParams(
    kabupatenId = kabupatenId.toShort(),
    kode = kode,
    nama = nama,
    createdBy = createdBy
)