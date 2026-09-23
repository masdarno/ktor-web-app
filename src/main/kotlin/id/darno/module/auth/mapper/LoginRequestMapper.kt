package id.darno.module.auth.mapper

import id.darno.module.auth.dto.LoginRequest
import id.darno.module.auth.model.LoginParams

fun LoginRequest.toFormData() = mapOf(
    "username" to username
)

fun LoginRequest.toLoginParams() = LoginParams(
    username = username,
    password = password
)