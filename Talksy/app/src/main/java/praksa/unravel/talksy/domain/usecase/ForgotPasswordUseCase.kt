package praksa.unravel.talksy.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository

class ForgotPasswordUseCase(private val repository: AuthRepository) {
    suspend fun  invoke(email:String){
        return repository.sendPasswordResetEmail(email)
    }
}