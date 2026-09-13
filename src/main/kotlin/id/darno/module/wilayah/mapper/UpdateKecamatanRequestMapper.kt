package id.darno.module.wilayah.mapper

import id.darno.module.wilayah.dto.UpdateKecamatanRequest
import id.darno.module.wilayah.model.UpdateKecamatanParams

fun UpdateKecamatanRequest.toUpdateKecamatanParams(
    updatedBy: Short
) = UpdateKecamatanParams(
    kabupatenId = kabupatenId,
    kode = kode,
    nama = nama,
    isActive = isActive,
    updatedBy = updatedBy
)