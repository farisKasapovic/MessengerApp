package praksa.unravel.talksy.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.model.User
import javax.inject.Inject

class AddUserToDatabaseUseCase @Inject constructor (private val repository: AuthRepository) {

    suspend fun invoke(user:User){
        repository.addUserToDatabase(user)
    }
}