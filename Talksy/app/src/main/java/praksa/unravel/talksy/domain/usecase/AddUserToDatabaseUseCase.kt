package praksa.unravel.talksy.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.model.User

class AddUserToDatabaseUseCase(private val repository: AuthRepository) {

    suspend fun invoke(user:User){
        repository.addUserToDatabase(user)
    }
}