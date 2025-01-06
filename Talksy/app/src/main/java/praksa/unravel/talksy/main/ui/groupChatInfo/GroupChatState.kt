package praksa.unravel.talksy.main.ui.groupChatInfo

import praksa.unravel.talksy.model.User

sealed class GroupChatState {
    object Loading : GroupChatState()
    data class MembersLoaded(val members: List<User>) : GroupChatState()
    data class Error(val message: String) : GroupChatState()
}
