package praksa.unravel.talksy.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import praksa.unravel.talksy.domain.usecase.AddUserToDatabaseUseCase
import praksa.unravel.talksy.domain.usecase.LinkEmailAndPasswordUseCase
import praksa.unravel.talksy.domain.usecase.LinkPhoneNumberUseCase
import praksa.unravel.talksy.domain.usecase.RegisterUserInAuthUseCase
import praksa.unravel.talksy.domain.usecase.VerifyPhoneNumberWithCodeUseCase
import praksa.unravel.talksy.model.User
import javax.inject.Inject
@HiltViewModel
class CodeViewModel @Inject constructor(
    private val verifyPhoneNumberWithCodeUseCase: VerifyPhoneNumberWithCodeUseCase,
    private val addUserToDatabaseUseCase: AddUserToDatabaseUseCase,
    private val registerUserInAuthUseCase: RegisterUserInAuthUseCase,
    private val linkPhoneNumberUseCase: LinkPhoneNumberUseCase
) : ViewModel() {

    private val _registrationComplete = MutableLiveData<Boolean>()
    val registrationComplete: LiveData<Boolean> = _registrationComplete

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun verifyCodeAndRegister(
        code: String,
        verificationId: String?,
        email: String,
        password: String,
        username: String,
        phoneNumber: String
    ) {
        if (verificationId.isNullOrEmpty() || code.length != 6) {
            _errorMessage.value = "Invalid verification code."
            return
        }

        viewModelScope.launch {
            try {
                // Verify phone number
                val credential = verifyPhoneNumberWithCodeUseCase(verificationId, code)


                val userId = registerUserInAuthUseCase.invoke(email, password)


                //val phoneLinked = linkEmailAndPasswordUseCase(email, password)
                val phoneLinked = linkPhoneNumberUseCase.invoke(credential)
                if (!phoneLinked) {
                    _errorMessage.value = "Failed to link phone number. Verification might be incorrect."
                    return@launch
                }else {
                    // Save the user details in Firestore
                    val user = User( // id ne mora jer Firestore stora id automatski
                        email = email,
                        username = username,
                        phone = phoneNumber
                    )
                    addUserToDatabaseUseCase.invoke(user)
                    _registrationComplete.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}
