package praksa.unravel.talksy.main.domain.usecase

import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import praksa.unravel.talksy.main.model.Message
import javax.inject.Inject


class ObserveMessageUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    fun invoke(
        chatId: String,
        onMessagesChanged: (List<Message>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        repository.observeMessages(chatId, onMessagesChanged, onError)
    }
}
