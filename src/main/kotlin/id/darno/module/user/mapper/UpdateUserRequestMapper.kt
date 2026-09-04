package id.darno.module.user.mapper

import id.darno.module.user.dto.UpdateUserRequest
import id.darno.module.user.model.UpdateUserParams

fun UpdateUserRequest.toFormData() = mapOf(
    "name" to name,
    "alias" to alias,
    "username" to username,
    "email" to email,
    "roleId" to roleId
)

fun UpdateUserRequest.toUpdateUserParams() = UpdateUserParams(
    name = name,
    alias = alias,
    username = username,
    email = email,
    roleId = roleId
)