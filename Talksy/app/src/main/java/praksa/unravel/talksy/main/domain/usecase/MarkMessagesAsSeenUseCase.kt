package praksa.unravel.talksy.main.domain.usecase

import android.util.Log
import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import javax.inject.Inject

class MarkMessagesAsSeenUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    suspend operator fun invoke(chatId: String, userId: String) {
        repository.markMessagesAsSeen(chatId, userId)
    }
}
