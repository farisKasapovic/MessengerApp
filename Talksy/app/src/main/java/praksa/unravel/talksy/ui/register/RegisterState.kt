package praksa.unravel.talksy.ui.register

sealed class RegisterState {
    object Loading : RegisterState()
    data class Failed(val errorMessage: String) : RegisterState()
    data class VerificationIdSuccess(val verificationId: String): RegisterState()
    object EmailAlreadyExists : RegisterState()
    object UsernameAlreadyExists : RegisterState()
    object PhoneNumberAlreadyExists : RegisterState()
    object FacebookSuccess: RegisterState()
    object GoogleSuccess: RegisterState()
}




// Various state during registration

/*
*
* fun loginWithFacebook(token: AccessToken) {
    viewModelScope.launch {
        _registerState.emit(RegisterState.Loading)

        val result = loginWithFacebookUseCase.invoke(token)
        result.onSuccess { success ->
            if (success) {
                _registerState.emit(RegisterState.FacebookSuccess)
            } else {
                _registerState.emit(RegisterState.Failed("Facebook login failed"))
            }
        }.onFailure { error ->
            _registerState.emit(RegisterState.Failed(error.message ?: "Facebook login error"))
        }
    }
}
*
* */