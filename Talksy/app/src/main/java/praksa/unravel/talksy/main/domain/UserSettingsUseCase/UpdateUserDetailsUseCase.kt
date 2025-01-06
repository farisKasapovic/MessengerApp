package praksa.unravel.talksy.main.domain.UserSettingsUseCase

import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.main.data.repositories.UserRepository
import praksa.unravel.talksy.model.User
import javax.inject.Inject

class UpdateUserDetailsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<Unit> = userRepository.updateUser(user)
}
