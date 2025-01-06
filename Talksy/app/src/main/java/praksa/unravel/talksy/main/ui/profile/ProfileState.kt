package praksa.unravel.talksy.main.ui.profile

import com.google.firebase.Timestamp
import praksa.unravel.talksy.main.model.Chat
import praksa.unravel.talksy.main.model.Message
import praksa.unravel.talksy.main.ui.chat.DirectMessageState
import praksa.unravel.talksy.model.User

sealed class ProfileState {
    object Loading : ProfileState()
    data class UserSuccess(val user: User) : ProfileState()
    data class UserStatus(val pair: Pair<String, Pair<Boolean, Timestamp?>>) : ProfileState()
    data class ImagesSuccess(val imageUrls: List<String>) : ProfileState()
    data class Error(val message: String) : ProfileState()
}
