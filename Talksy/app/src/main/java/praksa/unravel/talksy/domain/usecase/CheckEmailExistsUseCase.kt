package praksa.unravel.talksy.domain.usecase

import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.common.result.Result
import javax.inject.Inject

class CheckEmailExistsUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(email: String): Flow<Result<Boolean>> {
        return repository.checkEmailExists(email)
    }
}