package praksa.unravel.talksy.ui.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import praksa.unravel.talksy.ui.register.RegisterState
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
            try {
                val success = loginUserUseCase.invoke(email, password)
                _loginState.value = LoginState.Success("Login successful!")
            } catch (e: Exception) {
                //_loginSuccess.value = false
                _loginState.value = LoginState.Error(e.message ?: "An unknown error occurred")
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
                val emailExists = checkEmailExistsUseCase.invoke(email)
                if (!emailExists) {
                    _loginState.value = LoginState.ResetProblem("Email does not exist in our database")
                    _loginState.value = LoginState.Loading //reset stanja
                } else {
                    _loginState.value = LoginState.ResetSuccess("Check your email for password reset instructions")
                forgotPasswordUseCase.invoke(email)
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.ResetProblem(e.message ?: "An unknown error occurred")
                _loginState.value = LoginState.Loading //reset stanja
            }
        }
    }

}

