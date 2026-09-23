package id.darno.module.wilayah.mapper

import id.darno.module.wilayah.database.dao.ProvinsiEntity
import id.darno.module.wilayah.domain.ProvinsiDomain

fun ProvinsiEntity.toProvinsiDomain() = ProvinsiDomain(
    id = id.value,
    kode = kode,
    nama = nama,
    isActive = isActive
)