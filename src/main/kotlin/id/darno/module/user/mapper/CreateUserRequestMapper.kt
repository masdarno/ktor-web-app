package id.darno.module.user.mapper

import id.darno.core.model.DefaultValues
import id.darno.module.user.dto.CreateUserRequest
import id.darno.module.user.model.CreateUserParams

fun CreateUserRequest.toFormData() = mapOf(
    "name" to name,
    "alias" to alias,
    "username" to username,
    "email" to email,
    "roleId" to roleId
)

fun CreateUserRequest.toCreateUserParams() = CreateUserParams(
    name = name,
    alias = alias,
    username = username,
    password = username, // Default password = username
    email = email,
    photo = DefaultValues.DEFAULT_PHOTO,
    genderId = DefaultValues.DEFAULT_GENDER,
    roleId = roleId
)
