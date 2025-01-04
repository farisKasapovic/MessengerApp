package praksa.unravel.talksy.main.domain.usecase

import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import javax.inject.Inject

class FetchImageMessagesUseCase @Inject constructor(
    private val directMessageRepository: DirectMessageRepository
) {
    suspend operator fun invoke(chatId: String): List<String> {
        return directMessageRepository.fetchImageMessages(chatId)
    }
}
