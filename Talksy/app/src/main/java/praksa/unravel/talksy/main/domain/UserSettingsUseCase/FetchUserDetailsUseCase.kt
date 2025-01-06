package praksa.unravel.talksy.main.domain.UserSettingsUseCase


import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.main.data.repositories.UserRepository
import praksa.unravel.talksy.model.User
import javax.inject.Inject

class FetchUserDetailsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<Result<User>> = userRepository.getCurrentUser()
}
