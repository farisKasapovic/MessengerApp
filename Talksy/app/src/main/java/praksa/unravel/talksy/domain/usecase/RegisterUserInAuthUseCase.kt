package praksa.unravel.talksy.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.common.exception.Result

class RegisterUserInAuthUseCase(private val repository: AuthRepository) {

    suspend fun invoke(email:String,password:String):Result<String>{
      return  repository.registerUserInAuth(email,password)
    }

}