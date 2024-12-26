package praksa.unravel.talksy.main.domain.usecase


import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import praksa.unravel.talksy.main.model.Message
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    suspend operator fun invoke(chatId: String, message: Message) {
        repository.sendMessage(chatId, message)
    }
}
