package praksa.unravel.talksy.main.ui.contacts


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import praksa.unravel.talksy.common.result.Result
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

    init {
        fetchContacts()
    }

    private fun fetchContacts() {
        viewModelScope.launch {
            getContactsUseCase()
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            if (result.data.isEmpty()) {
                                _state.value = ContactsState.Empty
                            } else {
                                _state.value = ContactsState.Success(result.data)
                            }
                        }
                        is Result.Failure -> {
                            _state.value = ContactsState.Error(result.error.message ?: "Greška pri dohvatanu kontakata")
                        }
                    }
                }
        }
    }

    fun getProfilePictureUrl(userId: String): Flow<Result<String?>> {
        return getProfilePictureUrlUseCase(userId)
    }

    fun createOrFetchChat(contactId: String): Flow<Result<String>> {
        return createChatUseCase(contactId)
    }
}
