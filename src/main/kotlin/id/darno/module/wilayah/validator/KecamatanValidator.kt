package id.darno.module.wilayah.validator

import id.darno.core.validation.FieldError
import id.darno.core.validation.FieldValidator
import id.darno.core.validation.FormValidator
import id.darno.core.validation.rules.length
import id.darno.core.validation.rules.notEmpty
import id.darno.core.validation.rules.positive
import id.darno.module.wilayah.dto.CreateKecamatanRequest
import id.darno.module.wilayah.dto.UpdateKecamatanRequest

object CreateKecamatanValidator :
    FormValidator<CreateKecamatanRequest> {

    override fun validate(
        value: CreateKecamatanRequest
    ): List<FieldError> {

        val validator = FieldValidator()

        validator.positive(
            value = value.kabupatenId.toInt(),
            field = "kabupatenId",
            message = "Kabupaten harus dipilih"
        )

        validator.notEmpty(
            value = value.kode,
            field = "kode",
            message = "Kode kecamatan harus diisi"
        )

        validator.length(
            value = value.kode,
            field = "kode",
            length = 6,
            message = "Kode kecamatan harus 6 karakter"
        )

        validator.notEmpty(
            value = value.nama,
            field = "nama",
            message = "Nama kecamatan harus diisi"
        )

        return validator.errors
    }
}


object UpdateKecamatanValidator :
    FormValidator<UpdateKecamatanRequest> {

    override fun validate(
        value: UpdateKecamatanRequest
    ): List<FieldError> {

        val validator = FieldValidator()

        validator.positive(
            value = value.kabupatenId.toInt(),
            field = "kabupatenId",
            message = "Kabupaten harus dipilih"
        )

        validator.notEmpty(
            value = value.kode,
            field = "kode",
            message = "Kode kecamatan harus diisi"
        )

        validator.length(
            value = value.kode,
            field = "kode",
            length = 6,
            message = "Kode kecamatan harus 6 karakter"
        )

        validator.notEmpty(
            value = value.nama,
            field = "nama",
            message = "Nama kecamatan harus diisi"
        )

        return validator.errors
    }
}