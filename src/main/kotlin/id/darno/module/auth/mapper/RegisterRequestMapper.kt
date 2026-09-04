package id.darno.module.auth.mapper

import id.darno.core.model.DefaultValues
import id.darno.module.auth.dto.RegisterRequest
import id.darno.module.user.model.CreateUserParams

fun RegisterRequest.toFormData() = mapOf(
    "name" to name,
    "username" to username,
    "email" to email
)

fun RegisterRequest.toCreateUserParams() = CreateUserParams(
    name = name,
    alias = name, // Default = name
    username = username,
    password = password, // masih plain password
    email = email,
    photo = DefaultValues.DEFAULT_PHOTO,
    genderId = DefaultValues.DEFAULT_GENDER,
    roleId = DefaultValues.DEFAULT_ROLE
)