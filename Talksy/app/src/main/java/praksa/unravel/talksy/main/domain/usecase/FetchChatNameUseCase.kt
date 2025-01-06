package praksa.unravel.talksy.main.domain.usecase

import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import javax.inject.Inject
import praksa.unravel.talksy.common.result.Result

class FetchChatNameUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    operator fun invoke(chatId: String): Flow<Result<String?>> {
        return repository.fetchChatName(chatId)
    }
}
