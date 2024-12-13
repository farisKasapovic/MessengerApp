package praksa.unravel.talksy.domain.usecases

import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.data.repositories.AuthRepository
import javax.inject.Inject
import praksa.unravel.talksy.common.result.Result

class DeleteUserFromAuthUseCase @Inject constructor(private val authRepository: AuthRepository) {

   operator fun invoke(): Flow<Result<Unit>> {
           return authRepository.deleteUser()
    }
}
