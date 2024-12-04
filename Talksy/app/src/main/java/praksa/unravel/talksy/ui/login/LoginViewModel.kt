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
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginWithFacebookUseCase: LoginWithFacebookUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val loginUserUseCase: LoginUserUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val checkEmailExistsUseCase: CheckEmailExistsUseCase
) : ViewModel() {


    /*private val _resetSuccess = MutableLiveData<Boolean>()
    val resetSuccess: LiveData<Boolean> get() = _resetSuccess

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage*/

    private val _resetSuccess = MutableStateFlow(false)
    val resetSuccess: StateFlow<Boolean> get() = _resetSuccess

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> get() = _loginSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    // Login with email and password
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val success = loginUserUseCase.invoke(email, password)
                _loginSuccess.value = success
            } catch (e: Exception) {
                _loginSuccess.value = false
                _errorMessage.value = e.message
            }
        }
    }

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

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                val emailExists = checkEmailExistsUseCase.invoke(email)
                if (!emailExists) {
                    _errorMessage.value = "Email does not exists in our database"

                } else {
                forgotPasswordUseCase.invoke(email)
                _resetSuccess.value = true
                }
            } catch (e: Exception) {
                _resetSuccess.value = false
                _errorMessage.value = e.message
            }
        }
    }

}

