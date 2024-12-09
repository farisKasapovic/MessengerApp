package praksa.unravel.talksy.ui.login


sealed class LoginState {
    object Loading : LoginState()
    data class Success(val message: String) : LoginState()
    data class Error(val errorMessage: String) : LoginState()
    data class ResetSuccess(val resetMessage:String) : LoginState()
    data class ResetProblem(val resetProblem:String): LoginState()
}

