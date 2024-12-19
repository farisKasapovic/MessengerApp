package praksa.unravel.talksy.domain.usecase

import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.model.User
import praksa.unravel.talksy.common.result.Result
import javax.inject.Inject

class UpdateUserInfoUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(user:User, firstName:String,lastName:String,bio:String): Flow<Result<Unit>> {
        return repository.updateUserData(user,firstName,lastName,bio)
    }
}


