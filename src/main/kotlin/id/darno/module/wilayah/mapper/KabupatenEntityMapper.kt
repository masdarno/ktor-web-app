package id.darno.module.wilayah.mapper

import id.darno.module.wilayah.database.dao.KabupatenEntity
import id.darno.module.wilayah.domain.KabupatenDomain

fun KabupatenEntity.toKabupatenDomain() = KabupatenDomain(
    id = id.value,
    provinsiId = provinsi.id.value,
    provinsiNama = provinsi.nama,
    kode = kode,
    nama = nama,
    isActive = isActive
)