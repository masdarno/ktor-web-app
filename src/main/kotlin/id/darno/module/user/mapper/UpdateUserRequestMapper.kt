package id.darno.module.user.mapper

import id.darno.module.user.dto.UpdateUserRequest
import id.darno.module.user.model.UpdateUserParams

fun UpdateUserRequest.toFormData() = mapOf(
    "nama" to nama,
    "alias" to alias,
    "username" to username,
    "email" to email,
    "roleId" to roleId
)

fun UpdateUserRequest.toUpdateUserParams() = UpdateUserParams(
    nama = nama,
    alias = alias,
    username = username,
    email = email,
    roleId = roleId
)