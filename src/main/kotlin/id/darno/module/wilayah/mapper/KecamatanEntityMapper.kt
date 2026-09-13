package id.darno.module.wilayah.mapper

import id.darno.module.wilayah.database.dao.KecamatanEntity
import id.darno.module.wilayah.domain.KecamatanDomain

fun KecamatanEntity.toKecamatanDomain() = KecamatanDomain(
    id = id.value,
    kabupatenId = kabupaten.id.value,
    kabupatenNama = kabupaten.nama,
    kode = kode,
    nama = nama,
    isActive = isActive
)