package praksa.unravel.talksy.main.domain.usecase

import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import javax.inject.Inject

class DeleteMessageUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    operator fun invoke(messageId: String, chatId: String): Flow<Result<Unit>> {
        return repository.deleteMessage(messageId, chatId)
    }
}
