package praksa.unravel.talksy.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository

class CheckPhoneNumberExistsUseCase(private val repository: AuthRepository) {
    suspend fun invoke(phone:String):Boolean{
        return repository.checkPhoneNumberExists(phone)
    }
}