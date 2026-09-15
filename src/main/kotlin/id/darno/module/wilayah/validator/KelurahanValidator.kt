package id.darno.module.wilayah.validator

import id.darno.core.validation.FieldError
import id.darno.core.validation.FieldValidator
import id.darno.core.validation.FormValidator
import id.darno.core.validation.rules.length
import id.darno.core.validation.rules.notEmpty
import id.darno.core.validation.rules.positive
import id.darno.module.wilayah.dto.CreateKelurahanRequest
import id.darno.module.wilayah.dto.UpdateKelurahanRequest

object CreateKelurahanValidator :
    FormValidator<CreateKelurahanRequest> {

    override fun validate(
        value: CreateKelurahanRequest
    ): List<FieldError> {

        val validator = FieldValidator()

        validator.positive(
            value = value.kecamatanId.toInt(),
            field = "kecamatanId",
            message = "Kecamatan harus dipilih"
        )

        validator.notEmpty(
            value = value.kode,
            field = "kode",
            message = "Kode kelurahan harus diisi"
        )

        validator.length(
            value = value.kode,
            field = "kode",
            length = 10,
            message = "Kode kelurahan harus 10 karakter"
        )

        validator.notEmpty(
            value = value.nama,
            field = "nama",
            message = "Nama kelurahan harus diisi"
        )

        return validator.errors
    }
}


object UpdateKelurahanValidator :
    FormValidator<UpdateKelurahanRequest> {

    override fun validate(
        value: UpdateKelurahanRequest
    ): List<FieldError> {

        val validator = FieldValidator()

        validator.positive(
            value = value.kecamatanId.toInt(),
            field = "kecamatanId",
            message = "Kecamatan harus dipilih"
        )

        validator.notEmpty(
            value = value.kode,
            field = "kode",
            message = "Kode kelurahan harus diisi"
        )

        validator.length(
            value = value.kode,
            field = "kode",
            length = 10,
            message = "Kode kelurahan harus 10 karakter"
        )

        validator.notEmpty(
            value = value.nama,
            field = "nama",
            message = "Nama kelurahan harus diisi"
        )

        return validator.errors
    }
}