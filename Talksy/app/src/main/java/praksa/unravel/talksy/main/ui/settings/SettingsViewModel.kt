package praksa.unravel.talksy.main.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.main.domain.UserSettingsUseCase.FetchUserDetailsUseCase
import praksa.unravel.talksy.main.domain.UserSettingsUseCase.UpdateUserDetailsUseCase
import praksa.unravel.talksy.main.domain.usecase.GetProfilePictureUrlUseCase
import praksa.unravel.talksy.model.User
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val fetchUserDetailsUseCase: FetchUserDetailsUseCase,
    private val updateUserDetailsUseCase: UpdateUserDetailsUseCase,
    private val getProfilePictureUrlUseCase: GetProfilePictureUrlUseCase
) : ViewModel() {

    private val _state = MutableLiveData<SettingsState>()
    val state: LiveData<SettingsState> = _state

    fun fetchCurrentUser() {
        _state.value = SettingsState.Loading
        viewModelScope.launch {
            fetchUserDetailsUseCase()
                .catch { exception ->
                    _state.value = SettingsState.Error("Failed to fetch user details: ${exception.message}")
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> _state.value = SettingsState.UserDetails(result.data)
                        is Result.Failure -> _state.value = SettingsState.Error("Failed to fetch user details: ${result.error.message}")
                    }
                }
        }
    }

    fun updateUser(user: User) {
        _state.value = SettingsState.Loading
        viewModelScope.launch {
            val result = updateUserDetailsUseCase(user)
            if (result is Result.Success) {
                _state.value = SettingsState.UpdateSuccess("Profile updated successfully")
            } else if (result is Result.Failure) {
                _state.value = SettingsState.Error("Failed to update profile: ${result.error.message}")
            }
        }
    }

    fun fetchProfilePicture(userId: String) {
        _state.value = SettingsState.Loading
        viewModelScope.launch {
            getProfilePictureUrlUseCase(userId)
                .catch { exception ->
                    _state.value = SettingsState.Error("Failed to fetch profile picture: ${exception.message}")
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> _state.value = SettingsState.ProfilePictureUrl(result.data)
                        is Result.Failure -> _state.value = SettingsState.ProfilePictureUrlError("Failed to fetch profile picture: ${result.error.message}")
                    }
                }
        }
    }
}
