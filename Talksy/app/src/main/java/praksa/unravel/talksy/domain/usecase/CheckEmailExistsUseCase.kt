package praksa.unravel.talksy.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository

class CheckEmailExistsUseCase(private val repository: AuthRepository) {

    suspend /*operator*/ fun invoke(email: String): Boolean {
        return repository.checkEmailExists(email)
    }
}

// Done