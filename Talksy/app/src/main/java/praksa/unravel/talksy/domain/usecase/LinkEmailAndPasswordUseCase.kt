package praksa.unravel.talksy.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.common.exception.Result


class LinkEmailAndPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Boolean> {
        return repository.linkEmailAndPassword(email, password)
    }
}
