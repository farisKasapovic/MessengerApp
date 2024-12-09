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

