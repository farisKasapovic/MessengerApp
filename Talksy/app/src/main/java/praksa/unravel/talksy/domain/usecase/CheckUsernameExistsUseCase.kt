package praksa.unravel.talksy.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.common.result.Result
import javax.inject.Inject

class CheckUsernameExistsUseCase @Inject constructor(private val repository: AuthRepository) {

    suspend /*operator*/ fun invoke(username: String): Result<Boolean> {
        return repository.checkUsernameExists(username)
    }
}

//Done