package praksa.unravel.talksy.main.domain.usecase



import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import javax.inject.Inject

class DeleteMessageUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    suspend operator fun invoke(messageId: String,chatId:String) {
        repository.deleteMessage(messageId,chatId)
    }
}
