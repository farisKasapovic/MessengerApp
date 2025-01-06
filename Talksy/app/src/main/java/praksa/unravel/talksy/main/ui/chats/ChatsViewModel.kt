package praksa.unravel.talksy.main.ui.chats

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import praksa.unravel.talksy.common.result.Result

import praksa.unravel.talksy.main.domain.usecase.GetProfilePictureUrlUseCase
import praksa.unravel.talksy.main.domain.usecase.GetUnreadCountUseCase

import praksa.unravel.talksy.main.domain.usecase.ObserveChatsUseCase
import praksa.unravel.talksy.main.model.Chat
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val getProfilePictureUrlUseCase: GetProfilePictureUrlUseCase,
    private val observeChatsUseCase: ObserveChatsUseCase,
    private val getUnreadCountUseCase: GetUnreadCountUseCase
) : ViewModel() {

    private val _chatsState = MutableLiveData<List<Chat>>()
    val chatsState: LiveData<List<Chat>> = _chatsState


fun observeChats(userId: String) {
    observeChatsUseCase.invoke(
        userId = userId,
        onChatsUpdated = { updatedChats ->
            _chatsState.postValue(updatedChats)
        },
        onError = { error ->
            Log.e("ChatsViewModel", "Error observing chats: ${error.message}")
        }
    )
}

    fun getProfilePictureUrl(userId: String): Flow<Result<String?>> {
        return getProfilePictureUrlUseCase(userId)
    }

    suspend fun getUnreadCount(chatId: String, userId: String): Int {
        return getUnreadCountUseCase(chatId, userId)
            .first()
            .let { result ->
                when (result) {
                    is Result.Success -> result.data
                    is Result.Failure -> {
                        Log.e("ChatsViewModel", "Error fetching unread count: ${result.error.message}")
                        0
                    }
                }
            }
    }


}
