package praksa.unravel.talksy.ui.register

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import praksa.unravel.talksy.domain.usecase.*
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val checkEmailExistsUseCase: CheckEmailExistsUseCase,
    private val checkUsernameExistsUseCase: CheckUsernameExistsUseCase,
    private val sendVerificationCodeUseCase: SendVerificationCodeUseCase,
    private val loginWithFacebookUseCase: LoginWithFacebookUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val checkPhoneNumberExistsUseCase: CheckPhoneNumberExistsUseCase
) : ViewModel() {

    /*private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    private val _registrationState = MutableLiveData<String>()
    val registrationState: LiveData<String> get() = _registrationState

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage*/

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    private val _registrationState = MutableStateFlow<String?>(null)
    val registrationState: StateFlow<String?> = _registrationState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    fun startRegistration(
        email: String, password: String, username: String, phoneNumber: String, activity: FragmentActivity
    ) {
        //viewModelScope.launch {

            viewModelScope.launch {
            try {
                // Provjeram da li email postoji
                val emailExists = checkEmailExistsUseCase.invoke(email)
                if (emailExists) {
                    _errorMessage.value = "Email already exists."
                    return@launch
                }

                val usernameExists = checkUsernameExistsUseCase.invoke(username)
                if(usernameExists){
                    _errorMessage.value = "Username already exists"
                    return@launch
                }

                val phoneNumberExists = checkPhoneNumberExistsUseCase.invoke(phoneNumber)
                if(phoneNumberExists){
                    _errorMessage.value = "Number already exists"
                    return@launch
                }


                // Pokretanje telefonske autentifikacije
                sendVerificationCodeUseCase.invoke(phoneNumber, activity, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        _errorMessage.value = "Phone verification failed: ${e.message}"
                    }

                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        _registrationState.value = verificationId
                    }
                })
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
        //}
    }

    //Prijava sa facebook
fun loginWithFacebook(token:AccessToken){
    viewModelScope.launch {
        try {
            val success = loginWithFacebookUseCase.invoke(token)
            _loginSuccess.value = success
        } catch (e: Exception) {
            _errorMessage.value = e.message
        }
    }
}

    // Prijava sa Google-om
    fun loginWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                val success = loginWithGoogleUseCase.invoke(account)
                _loginSuccess.value = success
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }


}