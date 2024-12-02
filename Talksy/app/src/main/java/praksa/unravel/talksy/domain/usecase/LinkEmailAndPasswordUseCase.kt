package praksa.unravel.talksy.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository


class LinkEmailAndPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Boolean {
        return repository.linkEmailAndPassword(email, password)
    }
}
