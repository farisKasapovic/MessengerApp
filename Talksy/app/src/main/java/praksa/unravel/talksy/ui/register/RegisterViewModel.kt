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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.launch
import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.domain.usecase.*
import javax.inject.Inject
import kotlin.math.log

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
            // Validate inputs first
            val validationError =
                InputFieldValidator.validateInputs(email, password, username, phone)
            if (validationError != null) {
                Log.d("RegisterViewModel", "Validation error: $validationError")
                _registerState.emit(RegisterState.Failed(validationError))
                return@launch
            }

            _registerState.emit(RegisterState.Loading)

//            combine(
//                checkEmailExistsUseCase(email),
//                checkUsernameExistsUseCase(username),
//                checkPhoneNumberExistsUseCase(phone)
//            ) { emailExists, usernameExists, phoneExists ->
//                val doesEmailExist = when (emailExists) {
//                    is Result.Failure -> {
//                        _registerState.emit(RegisterState.Failed("Error checking email"))
//                        return@combine false
//                    }
//                    is Result.Success -> emailExists.data
//                }
//
//                val doesUsernameExist = when (usernameExists) {
//                    is Result.Failure -> {
//                        _registerState.emit(RegisterState.Failed("Error checking username"))
//                        return@combine false
//                    }
//                    is Result.Success -> usernameExists.data
//                }
//
//                val doesPhoneExist = when (phoneExists) {
//                    is Result.Failure -> {
//                        _registerState.emit(RegisterState.Failed("Error checking phone number"))
//                        return@combine false
//                    }
//                    is Result.Success -> phoneExists.data
//                }
//
//                // If any of the checks indicate existence, emit a failure state
//                if (doesEmailExist || doesUsernameExist || doesPhoneExist) {
//                    Log.d("RVM","values of $doesUsernameExist and $doesEmailExist")
//                    _registerState.emit(
//                        RegisterState.Failed(
//                            when {
//                                doesEmailExist -> "Email already exists"
//                                doesUsernameExist -> "Username already exists"
//                                doesPhoneExist -> "Phone number already exists"
//                                else -> "Validation failed"
//                            }
//                        )
//                    )
//                } else {
//                    // All checks passed, proceed to the next state
//                    _registerState.emit(RegisterState.Loading)
//                }
//            }





                val emailExists = checkEmailExistsUseCase(email).first().let { result ->
                    when (result) {
                        is Result.Success -> result.data
                        is Result.Failure -> {
                            _registerState.emit(RegisterState.Failed("Error checking email: ${result.error.message}"))
                            return@launch
                        }
                    }
                }
                if (emailExists) {
                    _registerState.emit(RegisterState.EmailAlreadyExists)
                    return@launch
                }

                val usernameExists = checkUsernameExistsUseCase(username).first().let { result ->
                    when (result) {
                        is Result.Success -> result.data
                        is Result.Failure -> {
                            _registerState.emit(RegisterState.Failed("Error checking username: ${result.error.message}"))
                            return@launch
                        }
                    }
                }
                if (usernameExists) {
                    _registerState.emit(RegisterState.UsernameAlreadyExists)
                    return@launch
                }

                val phoneExists = checkPhoneNumberExistsUseCase(phone).first().let { result ->
                    when (result) {
                        is Result.Success -> result.data
                        is Result.Failure -> {
                            _registerState.emit(RegisterState.Failed("Error checking phone number: ${result.error.message}"))
                            return@launch
                        }
                    }
                }
                if (phoneExists) {
                    _registerState.emit(RegisterState.PhoneNumberAlreadyExists)
                    return@launch
                }


            sendVerificationCodeUseCase.invoke(
                phone,
                activity,
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        Log.d("RegisterViewModel", "Verification completed")
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
                            _registerState.emit(RegisterState.VerificationIdSuccess(verificationId))
                        }
                    }
                }
            )

        }
    }
    
    fun loginWithFacebook(token: AccessToken) {
        viewModelScope.launch {
            loginWithFacebookUseCase(token)
                .collectLatest { fbResult ->
                    when (fbResult) {
                        is Result.Success -> {
                            _registerState.emit(RegisterState.FacebookSuccess)
                        }

                        is Result.Failure -> {
                            _registerState.emit(RegisterState.Failed("Facebook login failed: ${fbResult.error.message}"))
                        }
                    }
                }
        }
    }

    fun loginWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            loginWithGoogleUseCase(account).collectLatest { googleResult ->
                when (googleResult) {
                    is Result.Success -> {
                        _registerState.emit(RegisterState.GoogleSuccess)
                    }

                    is Result.Failure -> {
                        _registerState.emit(RegisterState.Failed("Google login failed: ${googleResult.error.message}"))
                    }
                }
            }
        }
    }


}