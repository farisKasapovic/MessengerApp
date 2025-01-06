package praksa.unravel.talksy.main.ui.profile

import android.provider.ContactsContract.Profile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import praksa.unravel.talksy.main.domain.usecase.FetchImageMessagesUseCase
import praksa.unravel.talksy.main.domain.usecase.GetProfilePictureUrlUseCase
import praksa.unravel.talksy.main.domain.usecase.GetUserInformationUseCase
import praksa.unravel.talksy.main.ui.chat.DirectMessageState
import javax.inject.Inject
import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.main.domain.UserStatusUsecase.GetUserStatusUseCase


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserInformationUseCase: GetUserInformationUseCase,
    private val getProfilePictureUrlUseCase: GetProfilePictureUrlUseCase,
    private val fetchImageMessagesUseCase: FetchImageMessagesUseCase,
    private val getUserStatusUseCase: GetUserStatusUseCase,
): ViewModel() {


    private val _profileState = MutableLiveData<ProfileState>()
    val profileState: LiveData<ProfileState> = _profileState



    fun getUserInformation(chatId: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            getUserInformationUseCase(chatId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val user = result.data
                        _profileState.value = ProfileState.UserSuccess(user)
                    }
                    is Result.Failure -> {
                        _profileState.value = ProfileState.Error("Failed to fetch user details: ${result.error.message}")
                    }
                }
            }
        }
    }


    fun getProfilePictureUrl(userId: String): Flow<Result<String?>> {
        return getProfilePictureUrlUseCase(userId)
    }

    fun fetchImageMessages(chatId: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            fetchImageMessagesUseCase(chatId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val imageUrls = result.data
                        _profileState.value = ProfileState.ImagesSuccess(imageUrls)
                    }
                    is Result.Failure -> {
                        _profileState.value = ProfileState.Error("Failed to fetch images: ${result.error.message}")
                    }
                }
            }
        }
    }

    fun fetchUserStatus(userId: String) {
        viewModelScope.launch {
            try {
                getUserStatusUseCase(userId).collect { userStatus ->
                    val status = Pair(userId, userStatus) // Create a pair of userId and status
                    _profileState.postValue(ProfileState.UserStatus(status))

                }
            } catch (e: Exception) {
                _profileState.postValue(ProfileState.Error("Error fetching user status: ${e.message}"))
            }
        }
    }






}