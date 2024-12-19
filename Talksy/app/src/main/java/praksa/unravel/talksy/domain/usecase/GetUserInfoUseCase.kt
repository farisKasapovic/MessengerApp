package praksa.unravel.talksy.domain.usecase

import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.model.User
import praksa.unravel.talksy.common.result.Result
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(): Flow<Result<User>> {
        return repository.getUserData()
    }
}
