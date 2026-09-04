package id.darno.module.unit.mapper

import id.darno.module.unit.database.dao.UnitEntity
import id.darno.module.unit.domain.UnitDomain

fun UnitEntity.toUnitDomain(): UnitDomain {
    return UnitDomain(
        id = this.id.value,
        name = this.name
    )
}