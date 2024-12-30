package praksa.unravel.talksy.main.domain.usecase

import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import javax.inject.Inject

class GetUnreadCountUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    suspend operator fun invoke(chatId: String, userId: String): Int {
        return repository.getUnreadCount(chatId, userId)
    }
}
