package praksa.unravel.talksy.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.common.exception.Result

class CheckEmailExistsUseCase(private val repository: AuthRepository) {

    suspend /*operator*/ fun invoke(email: String): Result<Boolean> {
        return repository.checkEmailExists(email)
    }
}

// Done