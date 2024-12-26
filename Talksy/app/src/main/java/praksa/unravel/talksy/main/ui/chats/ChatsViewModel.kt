package praksa.unravel.talksy.main.ui.chats

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import praksa.unravel.talksy.main.domain.usecase.FetchChatsWithMessagesUseCase
import praksa.unravel.talksy.main.domain.usecase.FetchChatsWithUserDetailsUseCase
import praksa.unravel.talksy.main.domain.usecase.GetProfilePictureUrlUseCase
import praksa.unravel.talksy.main.domain.usecase.Najnoviji
import praksa.unravel.talksy.main.model.Chat
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val getProfilePictureUrlUseCase: GetProfilePictureUrlUseCase,
    private val najnoviji: Najnoviji
) : ViewModel() {

    private val _chatsState = MutableLiveData<List<Chat>>()
    val chatsState: LiveData<List<Chat>> = _chatsState

//    fun fetchChats(userId: String) {
//        viewModelScope.launch {
//            try {
//               // val chats = fetchChatsWithMessagesUseCase(userId)
//                val chats = fetchChatsWithUserDetailsUseCase(userId)
//                _chatsState.postValue(chats)
//
//            } catch (e: Exception) {
//                Log.e("ChatsViewModel", "Error fetching chats: ${e.message}")
//                _chatsState.postValue(emptyList())
//            }
//        }
//    }
fun observeChats(userId: String) {
    najnoviji.invoke(
        userId = userId,
        onChatsUpdated = { updatedChats ->
            _chatsState.postValue(updatedChats)
        },
        onError = { error ->
            Log.e("ChatsViewModel", "Error observing chats: ${error.message}")
        }
    )
}

    suspend fun getProfilePictureUrl(userId: String):String?{
        return try{
                getProfilePictureUrlUseCase(userId)
        }catch (e: Exception){
            null
        }
    }

}
