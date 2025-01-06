package praksa.unravel.talksy.main.ui.settings

import praksa.unravel.talksy.model.User

sealed class SettingsState {
    object Loading : SettingsState()
    data class UserDetails(val user: User) : SettingsState()
    data class ProfilePictureUrl(val url: String?) : SettingsState()
    data class UpdateSuccess(val message: String) : SettingsState()
    data class Error(val errorMessage: String) : SettingsState()
    data class ProfilePictureUrlError(val errorMessage:String): SettingsState()
}
