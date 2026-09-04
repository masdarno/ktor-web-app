package id.darno.module.auth.mapper

import id.darno.core.model.DefaultValues
import id.darno.module.auth.dto.RegisterRequest
import id.darno.module.user.model.CreateUserParams

fun RegisterRequest.toFormData() = mapOf(
    "nama" to nama,
    "username" to username,
    "email" to email
)

fun RegisterRequest.toCreateUserParams() = CreateUserParams(
    nama = nama,
    alias = nama, // Default = nama
    username = username,
    password = password, // masih plain password
    email = email,
    photo = DefaultValues.DEFAULT_PHOTO,
    genderId = DefaultValues.DEFAULT_GENDER,
    roleId = DefaultValues.DEFAULT_ROLE
)