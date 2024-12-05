package praksa.unravel.talksy.ui.login

import com.facebook.login.Login

sealed class LoginState {
    object Idle : LoginState() // Initial state
    object Loading : LoginState()
    data class Success(val message: String) : LoginState()
    data class Error(val errorMessage: String) : LoginState()
    data class ResetSuccess(val resetMessage:String) : LoginState()
    data class ResetProblem(val resetProblem:String): LoginState()
}


/*

loginViewModel.loginState.collect { state ->
    when (state) {
        is LoginState.Idle -> showIdleState()
        is LoginState.Loading -> showLoadingIndicator()
        is LoginState.Success -> showSuccessMessage(state.message)
        is LoginState.Error -> showErrorMessage(state.errorMessage)
    }
}
sealed class Result <out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val error: Throwable) : Result<Nothing>()
}
* */