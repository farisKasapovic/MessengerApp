package praksa.unravel.talksy.main.ui.chat

import com.google.firebase.Timestamp
import praksa.unravel.talksy.main.model.Chat
import praksa.unravel.talksy.main.model.Message
import praksa.unravel.talksy.model.User

sealed class DirectMessageState {
    object Loading : DirectMessageState()
    data class UserSuccess(val user: User) : DirectMessageState()
    data class MessagesSuccess(val messages: List<Message>) : DirectMessageState()
    data class Error(val message: String) : DirectMessageState()
    data class UserStatus(val pair: Pair<String,Pair<Boolean,Timestamp?>>): DirectMessageState()
    data class ChatsSuccess(val chats: List<Chat>) : DirectMessageState()
}