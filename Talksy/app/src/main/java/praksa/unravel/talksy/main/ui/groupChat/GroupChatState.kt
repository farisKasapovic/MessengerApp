package praksa.unravel.talksy.main.ui.groupChat

import praksa.unravel.talksy.main.model.Contact

sealed class GroupChatState {
    object Loading : GroupChatState()
    object Empty : GroupChatState()
    data class Success(val contacts: List<Contact>) : GroupChatState()
    data class Error(val message: String) : GroupChatState()
}
