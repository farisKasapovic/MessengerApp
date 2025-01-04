package praksa.unravel.talksy.main.domain.GroupMessageUseCase

import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import praksa.unravel.talksy.main.model.Message
import javax.inject.Inject

class SendGroupMessageUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    suspend operator fun invoke(chatId: String, message: Message) {
        repository.sendGroupMessage(chatId, message)
    }
}
