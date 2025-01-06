package praksa.unravel.talksy.main.domain.usecase

import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import javax.inject.Inject

class IsGroupChatUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    operator fun invoke(chatId: String): Flow<Result<Boolean>> {
        return repository.isGroupChat(chatId)
    }
}
