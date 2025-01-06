package praksa.unravel.talksy.ui.moreinfo

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.domain.usecase.GetUserInfoUseCase
import praksa.unravel.talksy.domain.usecase.UpdateUserInfoUseCase
import praksa.unravel.talksy.domain.usecase.UploadProfilePictureUseCase
import praksa.unravel.talksy.model.User
import praksa.unravel.talksy.ui.logout.MoreInfoState
import javax.inject.Inject

@HiltViewModel
class MoreInfoViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val updateUserInfoUseCase: UpdateUserInfoUseCase,
    private val uploadProfilePictureUseCase: UploadProfilePictureUseCase
) : ViewModel() {

    private val _userState = MutableStateFlow<MoreInfoState>(MoreInfoState.Loading)
    val userState: StateFlow<MoreInfoState> = _userState

    private val _profilePictureUrl = MutableStateFlow<String?>(null)
    val profilePictureUrl: StateFlow<String?> = _profilePictureUrl

    init {
        fetchUserInfo()
    }

    private fun fetchUserInfo() {
        viewModelScope.launch {
            getUserInfoUseCase().first().let { result ->
                _userState.value = when (result) {
                    is Result.Success -> MoreInfoState.Success(result.data)
                    is Result.Failure -> MoreInfoState.Error(result.error.message ?: "Error")
                }
            }
        }
    }

    fun updateUserInfo(firstName: String, lastName: String, bio: String) {
        viewModelScope.launch {
            val user = (userState.value as? MoreInfoState.Success)?.user ?: return@launch
            updateUserInfoUseCase(user,firstName,lastName,bio).collect { result ->
                if (result is Result.Success) {
                    fetchUserInfo()
                    Log.d("MoreInfoViewModel","GRESKA: vrijednost resulta $result")
                }
            }
        }
    }

    fun uploadProfilePicture(uri: Uri) {
        viewModelScope.launch {
            uploadProfilePictureUseCase(uri).collect { result ->
                if (result is Result.Success) {
                    _profilePictureUrl.value = result.data
                }
            }
        }
    }
}

