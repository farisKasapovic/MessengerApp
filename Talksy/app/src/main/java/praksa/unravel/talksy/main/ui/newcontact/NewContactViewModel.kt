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
import praksa.unravel.talksy.main.domain.usecase.GetProfilePictureUrlUseCase
import praksa.unravel.talksy.main.model.Contact
import javax.inject.Inject

@HiltViewModel
class NewContactViewModel @Inject constructor(
    private val addContactUseCase: AddContactUseCase,
    private val checkUserExistsByPhoneOrUsername: CheckUserExistsByPhoneOrUsername
) : ViewModel() {

    private val _state = MutableLiveData<NewContactState>()
    val state: LiveData<NewContactState> = _state


    fun addContact(contact: Contact,phoneNumber:String,username: String) {
        viewModelScope.launch {
            val addedUserId = checkUserExistsByPhoneOrUsername(phoneNumber,username)
            if(addedUserId!=null){
                contact.id = addedUserId
                _state.value = NewContactState.Success
            } else {
                _state.value = NewContactState.Error("Korisnik ne koristi nasu aplikaciju")
                return@launch
            }
            _state.value = NewContactState.Loading
            try {
                addContactUseCase(contact,addedUserId)
                _state.value = NewContactState.Success
            } catch (e: Exception) {
                _state.value = NewContactState.Error(e.message ?: "Greška prilikom čuvanja kontakta")
            }
        }
    }



}
