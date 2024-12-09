package praksa.unravel.talksy.ui.signin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import praksa.unravel.talksy.domain.usecase.CheckEmailExistsUseCase
import praksa.unravel.talksy.domain.usecase.LoginUserUseCase
import praksa.unravel.talksy.domain.usecase.LoginWithFacebookUseCase
import praksa.unravel.talksy.domain.usecase.LoginWithGoogleUseCase
import praksa.unravel.talksy.domain.usecase.ForgotPasswordUseCase
import praksa.unravel.talksy.ui.login.LoginState
import praksa.unravel.talksy.common.exception.Result
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


    // Login with email and password
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            val loginSuccess = loginUserUseCase.invoke(email,password)
            Log.d("LoginViewModel", "u loginUser funkciiji $loginSuccess")
            when(loginSuccess){
                is Result.success -> {
                    _loginState.value = LoginState.Success("Login successful!")
                }
                is Result.failure -> {
                    _loginState.value = LoginState.Error( loginSuccess.error.message?: "An unknown error occurred")
                }
            }
        }
    }

    fun loginWithFacebook(token:AccessToken){
        viewModelScope.launch {
            try {
                val success = loginWithFacebookUseCase.invoke(token)
                _loginState.value = LoginState.Success("Facebook login successful!")
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }


    fun loginWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                val success = loginWithGoogleUseCase.invoke(account)
                _loginState.value = LoginState.Success("Google login successful!")
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading
                Log.d("LoginViewModel", "ResetPassword: State set to Loading")

                val emailExistsResult = checkEmailExistsUseCase.invoke(email)
                Log.d("LoginViewModel", "ResetPassword: Email check result = $emailExistsResult")

                when (emailExistsResult) {
                    is Result.success -> {
                        if (!emailExistsResult.data) {
                            _loginState.value = LoginState.ResetProblem("Email does not exist in our database")
                            Log.d("LoginViewModel", "ResetPassword: Email does not exist")
                        } else {
                            val forgotPasswordResult = forgotPasswordUseCase.invoke(email)
                            when (forgotPasswordResult) {
                                is Result.success -> {
//                                    if(forgotPasswordResult.data){
                                    _loginState.value = LoginState.ResetSuccess("Check your email for password reset instructions")
                                    Log.d("LoginViewModel", "ResetPassword: Reset successful")
                                //}
                                    }
                                is Result.failure -> {
                                    _loginState.value = LoginState.ResetProblem("An error occurred while resetting password")
                                    Log.d("LoginViewModel", "ResetPassword: Reset failed")
                                }
                            }
                        }
                    }
                    is Result.failure -> {
                        _loginState.value = LoginState.ResetProblem("Email does not exist in our database")
                        Log.d("LoginViewModel", "ResetPassword: Email check failed")
                    }
                }

            } catch (e: Exception) {
                _loginState.value = LoginState.ResetProblem(e.message ?: "An unknown error occurred")
                Log.e("LoginViewModel", "ResetPassword: Exception occurred", e)
            }
        }
    }


}

