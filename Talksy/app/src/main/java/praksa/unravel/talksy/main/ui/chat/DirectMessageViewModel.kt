package praksa.unravel.talksy.main.ui.chat

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.main.domain.GroupMessageUseCase.CreateGroupChatUseCase
import praksa.unravel.talksy.main.domain.GroupMessageUseCase.MarkGroupMessagesAsSeenUseCase
import praksa.unravel.talksy.main.domain.GroupMessageUseCase.SendGroupMessageUseCase
import praksa.unravel.talksy.main.domain.UserStatusUsecase.GetUserStatusUseCase
import praksa.unravel.talksy.main.domain.usecase.DeleteMessageUseCase
import praksa.unravel.talksy.main.domain.usecase.FetchChatNameUseCase
import praksa.unravel.talksy.main.domain.usecase.FetchMessagesUseCase
import praksa.unravel.talksy.main.domain.usecase.GetProfilePictureUrlUseCase
import praksa.unravel.talksy.main.domain.usecase.GetUserInformationUseCase
import praksa.unravel.talksy.main.domain.usecase.IsGroupChatUseCase
import praksa.unravel.talksy.main.domain.usecase.MarkMessagesAsSeenUseCase
import praksa.unravel.talksy.main.domain.usecase.ObserveChatsUseCase
import praksa.unravel.talksy.main.domain.usecase.ObserveMessageUseCase
import praksa.unravel.talksy.main.domain.usecase.RecordVoiceMessageUseCase
import praksa.unravel.talksy.main.domain.usecase.SendMessageUseCase
import praksa.unravel.talksy.main.domain.usecase.UploadImageAndSendMessageUseCase
import praksa.unravel.talksy.main.model.Message
import javax.inject.Inject

@HiltViewModel
class DirectMessageViewModel @Inject constructor(
    private val getUserInformationUseCase: GetUserInformationUseCase,
    private val getProfilePictureUrlUseCase: GetProfilePictureUrlUseCase,
    private val fetchMessagesUseCase: FetchMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val observeMessagesUseCase: ObserveMessageUseCase,
    private val getUserStatusUseCase: GetUserStatusUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val markMessagesAsSeenUseCase: MarkMessagesAsSeenUseCase,
    private val uploadImageAndSendMessageUseCase: UploadImageAndSendMessageUseCase,
    private val recordVoiceMessageUseCase: RecordVoiceMessageUseCase,
    private val fetchChatNameUseCase: FetchChatNameUseCase,
    private val isGroupChatUseCase: IsGroupChatUseCase,

    ) : ViewModel() {



    private val _directMessageState = MutableLiveData<DirectMessageState>()
    val directMessageState: LiveData<DirectMessageState> = _directMessageState


    fun getUserInformation(chatId: String) {
        viewModelScope.launch {
            getUserInformationUseCase(chatId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _directMessageState.postValue(DirectMessageState.UserSuccess(result.data))
                    }
                    is Result.Failure -> {
                        _directMessageState.postValue(
                            DirectMessageState.Error("Failed to fetch user details: ${result.error.message}")
                        )
                    }
                }
            }
        }
    }


    fun getProfilePictureUrl(userId: String): Flow<Result<String?>> {
        return getProfilePictureUrlUseCase(userId)
    }

    fun fetchMessages(chatId: String) {
        _directMessageState.value = DirectMessageState.Loading
        viewModelScope.launch {
            fetchMessagesUseCase(chatId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _directMessageState.value = DirectMessageState.MessagesSuccess(result.data)
                    }
                    is Result.Failure -> {
                        Log.e("DirectMessageViewModel", "Error fetching messages: ${result.error.message}")
                        _directMessageState.value = DirectMessageState.Error("Error fetching messages")
                    }
                }
            }
        }
    }


    fun sendMessage(chatId: String, text: String) {
        val senderId = FirebaseAuth.getInstance().currentUser?.uid
        if (senderId == null) {
            Log.e("DirectMessageViewModel", "User not logged in!")
            _directMessageState.value = DirectMessageState.Error("User not logged in!")
            return
        }
        val message = Message(
            text = text,
            senderId = senderId,
            timestamp = System.currentTimeMillis()

        )
        viewModelScope.launch {
            try {
                sendMessageUseCase(chatId, message)
                fetchMessages(chatId)
            } catch (e: Exception) {
                Log.e("DirectMessageViewModel", "Error sending message: ${e.message}")
                _directMessageState.value = DirectMessageState.Error("Error sending message")
            }
        }
    }

    fun observeMessages(chatId: String) {
        observeMessagesUseCase.invoke(
            chatId = chatId,
            onMessagesChanged = { messages ->
                _directMessageState.postValue(DirectMessageState.MessagesSuccess(messages))
            },
            onError = { error ->
                _directMessageState.postValue(DirectMessageState.Error("Error observing messages: ${error.message}"))
            }
        )
    }

    fun fetchUserStatus(userId: String) {
        viewModelScope.launch {
            try {
                getUserStatusUseCase(userId).collect { userStatus ->
                    val status = Pair(userId, userStatus) // Create a pair of userId and status
                    _directMessageState.postValue(DirectMessageState.UserStatus(status))
                }
            } catch (e: Exception) {
                _directMessageState.postValue(DirectMessageState.Error("Error fetching user status: ${e.message}"))
            }
        }
    }

    fun markMessagesAsSeen(chatId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                markMessagesAsSeenUseCase(chatId, userId)
            } catch (e: Exception) {
                Log.e("DirectMessageViewModel", "Error marking messages as seen: ${e.message}")
            }
        }
    }


    fun uploadImageAndSendMessage(chatId: String, imageUri: Uri) {
        val senderId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                uploadImageAndSendMessageUseCase(chatId, imageUri, senderId)
            } catch (e: Exception) {
                Log.e("DirectMessageViewModel", "Error sending image: ${e.message}")
            }
        }
    }


    fun deleteMessage(messageId: String, chatId: String) {
        viewModelScope.launch {
            try {
                deleteMessageUseCase(messageId,chatId)
            } catch (e: Exception) {
                Log.e("DirectMessageViewModel", "Error deleting message: ${e.message}")
            }
        }
    }

    fun sendVoiceMessage(chatId: String, filePath: String) {
        val senderId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                recordVoiceMessageUseCase(chatId, filePath, senderId)
            } catch (e: Exception) {
                Log.e("DirectMessageViewModel", "Error sending voice message: ${e.message}")
            }
        }
    }


    suspend fun getChatName(chatId: String): String? {
        return fetchChatNameUseCase(chatId)
            .first { result ->
                when (result) {
                    is Result.Success -> {
                        true
                    }
                    is Result.Failure -> {
                        Log.e("DirectMessageViewModel", "Error fetching chat name: ${result.error.message}")
                        true
                    }
                }
            }.let { result ->
                if (result is Result.Success) {
                    result.data
                } else {
                    null
                }
            }
    }

    fun checkIfGroupChat(chatId: String) {
        viewModelScope.launch {
            isGroupChatUseCase(chatId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _directMessageState.postValue(DirectMessageState.GroupCheckSuccess(result.data))
                    }
                    is Result.Failure -> {
                        _directMessageState.postValue(
                            DirectMessageState.Error("Failed to check group chat status: ${result.error.message}")
                        )
                    }
                }
            }
        }
    }




}

