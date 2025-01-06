package praksa.unravel.talksy.main.domain.usecase

import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import javax.inject.Inject
import praksa.unravel.talksy.common.result.Result

class GetUnreadCountUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    operator fun invoke(chatId: String, userId: String): Flow<Result<Int>> {
        return repository.getUnreadCount(chatId, userId)
    }
}
