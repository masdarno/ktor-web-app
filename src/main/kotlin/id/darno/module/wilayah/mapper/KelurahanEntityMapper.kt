package id.darno.module.wilayah.mapper

import id.darno.module.wilayah.database.dao.KelurahanEntity
import id.darno.module.wilayah.domain.KelurahanDomain

fun KelurahanEntity.toKelurahanDomain() = KelurahanDomain(
    id = id.value,
    kecamatanId = kecamatan.id.value,
    kecamatanNama = kecamatan.nama,
    kode = kode,
    nama = nama,
    isActive = isActive
)