package praksa.unravel.talksy.main.ui.chats

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel

import praksa.unravel.talksy.main.domain.usecase.GetProfilePictureUrlUseCase
import praksa.unravel.talksy.main.domain.usecase.GetUnreadCountUseCase

import praksa.unravel.talksy.main.domain.usecase.ObserveChatsUseCase
import praksa.unravel.talksy.main.model.Chat
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val getProfilePictureUrlUseCase: GetProfilePictureUrlUseCase,
    private val najnoviji: ObserveChatsUseCase,
    private val getUnreadCountUseCase: GetUnreadCountUseCase
) : ViewModel() {

    private val _chatsState = MutableLiveData<List<Chat>>()
    val chatsState: LiveData<List<Chat>> = _chatsState


fun observeChats(userId: String) {
    najnoviji.invoke(
        userId = userId,
        onChatsUpdated = { updatedChats ->
            _chatsState.postValue(updatedChats)
            Log.d("vazno","vrike ${updatedChats.size}")
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

    suspend fun getUnreadCount(chatId:String, userId: String): Int{
        return try{
            getUnreadCountUseCase(chatId, userId)
        }catch (e: Exception){
            Log.e("ChatsViewModel","Error fetching unread count")
            0
        }
    }

}
