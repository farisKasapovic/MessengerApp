package praksa.unravel.talksy.ui.register

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import praksa.unravel.talksy.common.result.Result
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
            val validationError =
                InputFieldValidator.validateInputs(email, password, username, phone)
            if (validationError != null) {
                Log.d("RegisterViewModel", "Uslo je u !=null validation error $validationError")
                _registerState.emit(RegisterState.Failed(validationError)) // Emit error
                return@launch
            }

            Log.d("RegisterViewModel", "Proslo validationError je null $validationError")
            _registerState.emit(RegisterState.Loading)

           //Remove try  catch
//            try {
                val emailResult = checkEmailExistsUseCase.invoke(email)
                Log.d("RegisterViewModel", "checkEmailExists $emailResult")
                when (emailResult) {
                    is Result.success -> {
                        if (emailResult.data) {
                            // Email već postoji
                            Log.d("RegisterViewModel", "Email already exists: ${emailResult.data}")
                            _registerState.emit(RegisterState.EmailAlreadyExists)
                            return@launch
                        }
                    }

                    else -> Unit
                }

                val usernameResult = checkUsernameExistsUseCase.invoke(username)
                when (usernameResult) {
                    is Result.success -> {
                        if (usernameResult.data) {
                            _registerState.emit(RegisterState.UsernameAlreadyExists)
                            Log.d("RegisterViewModel", "trebalo mi da emituj username:  $usernameResult")
                            return@launch
                        }
                    }

                    else -> Unit
                }

                val phoneResult = checkPhoneNumberExistsUseCase.invoke(phone)
                when (phoneResult) {
                    is Result.success -> {
                        if (phoneResult.data) {
                            _registerState.emit(RegisterState.PhoneNumberAlreadyExists)
                            Log.d("RegisterViewModel", "trebalo mi da emituj telefon:  $emailResult")
                            return@launch
                        }
                    }

                    else -> Unit
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
//            } catch (e: Exception) {
//                _registerState.emit(RegisterState.Failed(e.message ?: "Unknown error"))
//            }
        }
    }

    fun loginWithFacebook(token: AccessToken) {
        viewModelScope.launch {
            val fbResult = loginWithFacebookUseCase.invoke(token)
            when (fbResult) {
                is Result.success -> {
                    _registerState.emit(RegisterState.FacebookSuccess)
                    return@launch
                }
                is Result.failure -> {
                    _registerState.emit(RegisterState.Failed("Facebook login failed"))
                }
            }


        }

    }

    fun loginWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            val googleResult = loginWithGoogleUseCase.invoke(account)
            when (googleResult) {
                is Result.success -> {
                    _registerState.emit(RegisterState.GoogleSuccess)
                    return@launch
                }

                is Result.failure -> {
                    _registerState.emit(RegisterState.Failed("Google login failed"))
                }
            }
        }
    }
}