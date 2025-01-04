package praksa.unravel.talksy.main.domain.GroupMessageUseCase

import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import javax.inject.Inject

class MarkGroupMessagesAsSeenUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    suspend operator fun invoke(chatId: String, userId: String) {
        repository.markGroupMessagesAsSeen(chatId, userId)
    }
}
