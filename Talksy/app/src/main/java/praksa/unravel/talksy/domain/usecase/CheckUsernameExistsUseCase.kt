package praksa.unravel.talksy.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository

class CheckUsernameExistsUseCase(private val repository: AuthRepository) {

    suspend /*operator*/ fun invoke(username: String): Boolean {
        return repository.checkUsernameExists(username)
    }
}

//Done