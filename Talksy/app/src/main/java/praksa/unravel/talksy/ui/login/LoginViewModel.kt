package praksa.unravel.talksy.ui.signin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import praksa.unravel.talksy.domain.usecase.CheckEmailExistsUseCase
import praksa.unravel.talksy.domain.usecase.LoginUserUseCase
import praksa.unravel.talksy.domain.usecase.LoginWithFacebookUseCase
import praksa.unravel.talksy.domain.usecase.LoginWithGoogleUseCase
import praksa.unravel.talksy.domain.usecase.ForgotPasswordUseCase
import praksa.unravel.talksy.ui.login.LoginState
import praksa.unravel.talksy.common.result.Result
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginWithFacebookUseCase: LoginWithFacebookUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val loginUserUseCase: LoginUserUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val checkEmailExistsUseCase: CheckEmailExistsUseCase
) : ViewModel() {


    private val _loginState = MutableStateFlow<LoginState>(LoginState.Loading)
    val loginState: StateFlow<LoginState> = _loginState




    fun loginUser(email: String,password: String) {
        viewModelScope.launch {
            loginUserUseCase(email, password).collectLatest { loginResult ->
                when(loginResult){
                    is Result.Success -> {
                        _loginState.value = LoginState.Success("Login successful!")
                    }
                    is Result.Failure -> {
                        _loginState.value = LoginState.Error(loginResult.error.message ?: "Error occurred")
                    }
                }
            }
        }
    }


    fun loginWithFacebook(token: AccessToken) {
        viewModelScope.launch {
            loginWithFacebookUseCase(token).collectLatest { fbResult ->
                when (fbResult) {
                    is Result.Success -> {
                        _loginState.emit(LoginState.Success("Facebook login successful!"))
                    }

                    is Result.Failure -> {
                        _loginState.emit(LoginState.Error("Facebook login failed: ${fbResult.error.message}"))
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
                        _loginState.emit(LoginState.Success("Google login successful!"))
                    }

                    is Result.Failure -> {
                        _loginState.emit(LoginState.Error("Google login failed: ${googleResult.error.message}"))
                    }
                }
            }
        }
    }







    fun resetPassword(email: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            Log.d("LoginViewModel", "ResetPassword: State set to Loading")

            try {
                checkEmailExistsUseCase(email).collect { emailExistsResult ->
                    when (emailExistsResult) {
                        is Result.Success -> {
                            if (!emailExistsResult.data) {
                                _loginState.value = LoginState.ResetProblem("Email does not exist in our database")
                                Log.d("LoginViewModel", "ResetPassword: Email does not exist")
                            } else {
                                forgotPasswordUseCase(email).collect { forgotPasswordResult ->
                                    when (forgotPasswordResult) {
                                        is Result.Success -> {
                                            _loginState.value = LoginState.ResetSuccess("Check your email for password reset instructions")
                                            Log.d("LoginViewModel", "ResetPassword: Reset successful")
                                        }
                                        is Result.Failure -> {
                                            _loginState.value = LoginState.ResetProblem("An error occurred while resetting password")
                                            Log.d("LoginViewModel", "ResetPassword: Reset failed")
                                        }
                                    }
                                }
                            }
                        }
                        is Result.Failure -> {
                            _loginState.value = LoginState.ResetProblem("Email does not exist in our database")
                            Log.d("LoginViewModel", "ResetPassword: Email check failed")
                        }
                    }
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.ResetProblem("An unexpected error occurred")
                Log.d("LoginViewModel", "ResetPassword: Exception caught - ${e.message}")
            }
        }
    }



}

