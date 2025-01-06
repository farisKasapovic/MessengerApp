package praksa.unravel.talksy.main.domain.usecase

import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import javax.inject.Inject

class FetchImageMessagesUseCase @Inject constructor(
    private val directMessageRepository: DirectMessageRepository
) {
    suspend operator fun invoke(chatId: String): Flow<Result<List<String>>> {
        return directMessageRepository.fetchImageMessages(chatId)
    }
}
