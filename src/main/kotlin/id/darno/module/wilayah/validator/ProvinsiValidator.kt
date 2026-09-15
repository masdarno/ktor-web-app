package id.darno.module.wilayah.validator

import id.darno.core.validation.FieldError
import id.darno.core.validation.FieldValidator
import id.darno.core.validation.FormValidator
import id.darno.core.validation.rules.length
import id.darno.core.validation.rules.notEmpty
import id.darno.module.wilayah.dto.CreateProvinsiRequest
import id.darno.module.wilayah.dto.UpdateProvinsiRequest

object CreateProvinsiValidator :
    FormValidator<CreateProvinsiRequest> {

    override fun validate(
        value: CreateProvinsiRequest
    ): List<FieldError> {

        val validator = FieldValidator()

        validator.notEmpty(
            value = value.kode,
            field = "kode",
            message = "Kode provinsi harus diisi"
        )

        validator.length(
            value = value.kode,
            field = "kode",
            length = 2,
            message = "Kode provinsi harus 2 karakter"
        )

        validator.notEmpty(
            value = value.nama,
            field = "nama",
            message = "Nama provinsi harus diisi"
        )

        return validator.errors
    }
}


object UpdateProvinsiValidator :
    FormValidator<UpdateProvinsiRequest> {

    override fun validate(
        value: UpdateProvinsiRequest
    ): List<FieldError> {

        val validator = FieldValidator()

        validator.notEmpty(
            value = value.kode,
            field = "kode",
            message = "Kode provinsi harus diisi"
        )

        validator.length(
            value = value.kode,
            field = "kode",
            length = 2,
            message = "Kode provinsi harus 2 karakter"
        )

        validator.notEmpty(
            value = value.nama,
            field = "nama",
            message = "Nama provinsi harus diisi"
        )

        return validator.errors
    }
}