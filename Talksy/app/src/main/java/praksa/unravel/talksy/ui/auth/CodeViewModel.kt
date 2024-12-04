package praksa.unravel.talksy.ui.auth

import android.app.StartForegroundCalledOnStoppedServiceException
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.internal.Mutable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import praksa.unravel.talksy.domain.usecase.AddUserToDatabaseUseCase
import praksa.unravel.talksy.domain.usecase.LinkEmailAndPasswordUseCase
import praksa.unravel.talksy.domain.usecase.LinkPhoneNumberUseCase
import praksa.unravel.talksy.domain.usecase.RegisterUserInAuthUseCase
import praksa.unravel.talksy.domain.usecase.VerifyPhoneNumberWithCodeUseCase
import praksa.unravel.talksy.domain.usecases.DeleteUserFromAuthUseCase
import praksa.unravel.talksy.model.User
import javax.inject.Inject
@HiltViewModel
class CodeViewModel @Inject constructor(
    private val verifyPhoneNumberWithCodeUseCase: VerifyPhoneNumberWithCodeUseCase,
    private val addUserToDatabaseUseCase: AddUserToDatabaseUseCase,
    private val registerUserInAuthUseCase: RegisterUserInAuthUseCase,
    private val linkPhoneNumberUseCase: LinkPhoneNumberUseCase,
    private val deleteUserUseCase: DeleteUserFromAuthUseCase
) : ViewModel() {

//    private val _registrationComplete = MutableLiveData<Boolean>()
//    val registrationComplete: LiveData<Boolean> = _registrationComplete

//    private val _errorMessage = MutableLiveData<String?>()
//    val errorMessage: LiveData<String?> = _errorMessage

    private val _registrationComplete = MutableStateFlow(false)
    val registrationComplete: StateFlow<Boolean> = _registrationComplete

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage




    fun verifyCodeAndRegister(
        code: String,
        verificationId: String?,
        email: String,
        password: String,
        username: String,
        phoneNumber: String
    ) {
        if (verificationId.isNullOrEmpty() || code.length != 6) {
            Log.d("CodeViewModel","Uslo je u if uslove $verificationId i $code ")
            _errorMessage.value = "Invalid verification code."
            return
        }

        viewModelScope.launch {
            try {
                Log.d("CodeViewModel","Uslo je u CodeViewModel $verificationId  i $code")
                val credential = verifyPhoneNumberWithCodeUseCase(verificationId, code)

                Log.d("CodeViewModel","vrijednost credentiala $credential")
                val userId = registerUserInAuthUseCase.invoke(email, password)
                val phoneLinked = linkPhoneNumberUseCase.invoke(credential)
                Log.d("CodeViewModel","linkanje telefona $phoneLinked")
                if (!phoneLinked) {
                    _errorMessage.value = "Phone verification failed. Please try again."
                    //dodati ovdje delete user
                    return@launch
                }

                val user = User(
                    email = email,
                    username = username,
                    phone = phoneNumber,
                    profilePicture = ""
                )
                addUserToDatabaseUseCase.invoke(user)

                _registrationComplete.value = true
            } catch (e: Exception) {
                //OVDJE DODAJ DA OBRISES
                deleteUserUseCase.invoke()
                _errorMessage.value = e.message
            }
        }
    }

}
