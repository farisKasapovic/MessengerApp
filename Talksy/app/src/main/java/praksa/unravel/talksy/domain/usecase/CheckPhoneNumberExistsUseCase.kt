package praksa.unravel.talksy.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.common.exception.Result

class CheckPhoneNumberExistsUseCase(private val repository: AuthRepository) {
    suspend fun invoke(phone:String):Result<Boolean>{
        return repository.checkPhoneNumberExists(phone)
    }
}