package praksa.unravel.talksy.main.domain.usecase

import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.common.mapSuccess
import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import praksa.unravel.talksy.model.User
import javax.inject.Inject
import praksa.unravel.talksy.common.result.Result

class GetUserInformationUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    operator fun invoke(chatId: String): Flow<Result<User>> {
        return repository.getUserInformation(chatId).mapSuccess { user ->
            user ?: throw IllegalArgumentException("User not found") // Ensure non-null User
        }
    }
}
