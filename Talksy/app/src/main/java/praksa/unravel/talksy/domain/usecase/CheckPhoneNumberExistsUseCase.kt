package praksa.unravel.talksy.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.common.result.Result
import javax.inject.Inject

class CheckPhoneNumberExistsUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend fun invoke(phone:String): Result<Boolean> {
        return repository.checkPhoneNumberExists(phone)
    }
}