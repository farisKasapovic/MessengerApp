package praksa.unravel.talksy.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository

class RegisterUserInAuthUseCase(private val repository: AuthRepository) {

    suspend fun invoke(email:String,password:String){
        repository.registerUserInAuth(email,password)
    }

}