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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
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


    private val _registerState = MutableSharedFlow<RegisterState>(replay = 0)
    val registerState: SharedFlow<RegisterState> = _registerState


    fun startRegistration(
        email: String,
        password: String,
        username: String,
        phone: String,
        activity: FragmentActivity
    ) {
        viewModelScope.launch {
            val validationError = InputFieldValidator.validateInputs(email, password, username, phone)
            if (validationError != null) {
                _registerState.emit(RegisterState.Failed(validationError)) // Emit error
                return@launch
            }

            _registerState.emit(RegisterState.Loading)

            try {
                if (checkEmailExistsUseCase.invoke(email)) {
                    _registerState.emit(RegisterState.EmailAlreadyExists)
                    return@launch
                }
                if (checkUsernameExistsUseCase.invoke(username)) {
                    _registerState.emit(RegisterState.UsernameAlreadyExists)
                    return@launch
                }
                if (checkPhoneNumberExistsUseCase.invoke(phone)) {
                    _registerState.emit(RegisterState.PhoneNumberAlreadyExists)
                    return@launch
                }

                sendVerificationCodeUseCase.invoke(
                    phone,
                    activity,
                    object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            // Success
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            viewModelScope.launch {
                                _registerState.emit(RegisterState.Failed("Verification failed: ${e.message}"))
                            }
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            viewModelScope.launch {
                                _registerState.emit(
                                    RegisterState.VerificationIdSuccess(
                                        verificationId
                                    )
                                )
                            }
                        }
                    })
            } catch (e: Exception) {
                _registerState.emit(RegisterState.Failed(e.message ?: "Unknown error"))
            }
        }
    }

    fun loginWithFacebook(token: AccessToken) {
        viewModelScope.launch {
            _registerState.emit(RegisterState.Loading)
            try {
                val success = loginWithFacebookUseCase.invoke(token)
                if (success) {
                    _registerState.emit(RegisterState.FacebookSuccess)
                } else {
                    _registerState.emit(RegisterState.Failed("Facebook login failed"))
                }
            } catch (e: Exception) {
                _registerState.emit(RegisterState.Failed(e.message ?: "Facebook login error"))
            }
        }
    }

    fun loginWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _registerState.emit(RegisterState.Loading)
            try {
                val success = loginWithGoogleUseCase.invoke(account)
                if (success) {
                    _registerState.emit(RegisterState.GoogleSuccess)
                } else {
                    _registerState.emit(RegisterState.Failed("Google login error"))
                }
            } catch (e: Exception) {
                _registerState.emit(RegisterState.Failed(e.message ?: "Google login error"))
            }
        }
    }
}
