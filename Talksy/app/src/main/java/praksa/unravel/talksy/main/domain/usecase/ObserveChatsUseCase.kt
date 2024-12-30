package praksa.unravel.talksy.main.domain.usecase

import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import praksa.unravel.talksy.main.model.Chat
import javax.inject.Inject

class ObserveChatsUseCase @Inject constructor(
    private val directMessageRepository: DirectMessageRepository
) {
    fun invoke(
        userId: String,
        onChatsUpdated: (List<Chat>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        directMessageRepository.observeChats(userId, onChatsUpdated, onError)
    }
}
