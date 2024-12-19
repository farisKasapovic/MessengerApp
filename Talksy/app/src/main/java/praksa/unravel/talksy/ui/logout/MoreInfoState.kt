package praksa.unravel.talksy.ui.logout

import praksa.unravel.talksy.model.User

sealed class MoreInfoState {
    object Loading : MoreInfoState()
    data class Success(val user: User) : MoreInfoState()
    data class Error(val message: String) : MoreInfoState()
}