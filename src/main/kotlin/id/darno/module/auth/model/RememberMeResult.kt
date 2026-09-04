package id.darno.module.auth.model

import id.darno.core.session.model.UserSession

sealed class RememberMeResult {
    data class Authenticated(val session: UserSession) : RememberMeResult()
    object Invalid : RememberMeResult()
}
