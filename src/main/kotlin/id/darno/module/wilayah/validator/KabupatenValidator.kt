package id.darno.module.wilayah.validator

import id.darno.core.validation.FieldError
import id.darno.core.validation.FieldValidator
import id.darno.core.validation.FormValidator
import id.darno.core.validation.rules.length
import id.darno.core.validation.rules.notEmpty
import id.darno.core.validation.rules.positive
import id.darno.module.wilayah.dto.CreateKabupatenRequest
import id.darno.module.wilayah.dto.UpdateKabupatenRequest

object CreateKabupatenValidator :
    FormValidator<CreateKabupatenRequest> {

    override fun validate(
        value: CreateKabupatenRequest
    ): List<FieldError> {

        val validator = FieldValidator()

        validator.positive(
            value = value.provinsiId.toInt(),
            field = "provinsiId",
            message = "Provinsi harus dipilih"
        )

        validator.notEmpty(
            value = value.kode,
            field = "kode",
            message = "Kode kabupaten harus diisi"
        )

        validator.length(
            value = value.kode,
            field = "kode",
            length = 4,
            message = "Kode kabupaten harus 4 karakter"
        )

        validator.notEmpty(
            value = value.nama,
            field = "nama",
            message = "Nama kabupaten harus diisi"
        )

        return validator.errors
    }
}


object UpdateKabupatenValidator :
    FormValidator<UpdateKabupatenRequest> {

    override fun validate(
        value: UpdateKabupatenRequest
    ): List<FieldError> {

        val validator = FieldValidator()

        validator.positive(
            value = value.provinsiId.toInt(),
            field = "provinsiId",
            message = "Provinsi harus dipilih"
        )

        validator.notEmpty(
            value = value.kode,
            field = "kode",
            message = "Kode kabupaten harus diisi"
        )

        validator.length(
            value = value.kode,
            field = "kode",
            length = 4,
            message = "Kode kabupaten harus 4 karakter"
        )

        validator.notEmpty(
            value = value.nama,
            field = "nama",
            message = "Nama kabupaten harus diisi"
        )

        return validator.errors
    }
}