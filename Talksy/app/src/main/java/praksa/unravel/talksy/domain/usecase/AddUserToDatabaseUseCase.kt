package praksa.unravel.talksy.domain.usecase

import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.model.User
import javax.inject.Inject

class AddUserToDatabaseUseCase @Inject constructor (private val repository: AuthRepository) {

      operator fun invoke(user:User):Flow<Result<Unit>>{
       return  repository.addUserToDatabase(user)
    }
}