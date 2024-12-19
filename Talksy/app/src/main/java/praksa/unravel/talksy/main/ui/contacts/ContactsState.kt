package praksa.unravel.talksy.main.ui.contacts

import praksa.unravel.talksy.main.model.Contact

sealed class ContactsState {
    object Loading : ContactsState()
    data class Success(val contacts: List<Contact>) : ContactsState()
    data class Error(val message: String) : ContactsState()
    object Empty : ContactsState()
}
