package praksa.unravel.talksy.main.ui.profile

import android.provider.ContactsContract.Profile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import praksa.unravel.talksy.main.domain.usecase.FetchImageMessagesUseCase
import praksa.unravel.talksy.main.domain.usecase.GetProfilePictureUrlUseCase
import praksa.unravel.talksy.main.domain.usecase.GetUserInformationUseCase
import praksa.unravel.talksy.main.ui.chat.DirectMessageState
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserInformationUseCase: GetUserInformationUseCase,
    private val getProfilePictureUrlUseCase: GetProfilePictureUrlUseCase,
    private val fetchImageMessagesUseCase: FetchImageMessagesUseCase
): ViewModel() {


    private val _profileState = MutableLiveData<ProfileState>()
    val profileState: LiveData<ProfileState> = _profileState



    fun getUserInformation(chatId: String){
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try{
                val user = getUserInformationUseCase(chatId)
                if(user!=null){
                    _profileState.value = ProfileState.UserSuccess(user)
                } else {
                  _profileState.value = ProfileState.Error("User not found")
                }
            } catch (e: Exception){
                _profileState.value = ProfileState.Error("Failed to fetch user details ${e.message}")
            }
        }
    }


    suspend fun getProfilePictureUrl(userId: String): String?{
        return try{
            getProfilePictureUrlUseCase(userId)
        }catch(e:Exception){
            null
        }
    }

    fun fetchImageMessages(chatId: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val imageUrls = fetchImageMessagesUseCase(chatId)
                _profileState.value = ProfileState.ImagesSuccess(imageUrls)
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to fetch images: ${e.message}")
            }
        }
    }


}