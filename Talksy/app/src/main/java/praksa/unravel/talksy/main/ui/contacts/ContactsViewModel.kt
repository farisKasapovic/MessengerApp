package praksa.unravel.talksy.main.ui.contacts


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import praksa.unravel.talksy.main.domain.usecase.GetContactsUseCase
import praksa.unravel.talksy.main.domain.usecase.GetProfilePictureUrlUseCase
import praksa.unravel.talksy.main.domain.usecase.CreateChatUseCase
import javax.inject.Inject


@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase,
    private val getProfilePictureUrlUseCase: GetProfilePictureUrlUseCase,
    private val createChatUseCase: CreateChatUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ContactsState>(ContactsState.Loading)
    val state: StateFlow<ContactsState> = _state

    // init - svaki put kad se pokrene
    init {
        fetchContacts()
    }

    private fun fetchContacts() {
        viewModelScope.launch {
            _state.value = ContactsState.Loading
            try {

                val contacts = getContactsUseCase()
                if (contacts.isEmpty()) {
                    _state.value = ContactsState.Empty
                } else {
                    _state.value = ContactsState.Success(contacts)
                }
            } catch (e: Exception) {
                _state.value = ContactsState.Error(e.message ?: "Greška pri dohvatanu kontakata")
            }
        }
    }
    suspend fun getProfilePictureUrl(userId: String): String? {
        return try {
            getProfilePictureUrlUseCase(userId)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createOrFetchChat(contactId: String): String {
        return createChatUseCase.invoke(contactId)
    }



}
