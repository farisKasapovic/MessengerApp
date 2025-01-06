package praksa.unravel.talksy.main.ui.newcontact

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import praksa.unravel.talksy.main.domain.usecase.AddContactUseCase
import praksa.unravel.talksy.main.domain.usecase.CheckUserExistsByPhoneOrUsername
import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.main.model.Contact
import javax.inject.Inject

@HiltViewModel
class NewContactViewModel @Inject constructor(
    private val addContactUseCase: AddContactUseCase,
    private val checkUserExistsByPhoneOrUsername: CheckUserExistsByPhoneOrUsername
) : ViewModel() {

    private val _state = MutableLiveData<NewContactState>()
    val state: LiveData<NewContactState> = _state

    fun addContact(contact: Contact, phoneNumber: String, username: String) {
        viewModelScope.launch {
            checkUserExistsByPhoneOrUsername(phoneNumber, username)
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val addedUserId = result.data
                            if (addedUserId != null) {
                                contact.id = addedUserId
                                addContactUseCase(contact, addedUserId).collect { addResult ->
                                    when (addResult) {
                                        is Result.Success -> _state.postValue(NewContactState.Success)
                                        is Result.Failure -> _state.postValue(NewContactState.Error(addResult.error.message ?: "Greška"))
                                    }
                                }
                            } else {
                                _state.postValue(NewContactState.Error("Korisnik ne koristi našu aplikaciju"))
                            }
                        }
                        is Result.Failure -> _state.postValue(NewContactState.Error(result.error.message ?: "Greška"))
                    }
                }
        }
    }
}
