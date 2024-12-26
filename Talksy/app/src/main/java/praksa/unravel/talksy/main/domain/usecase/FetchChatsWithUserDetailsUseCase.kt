package praksa.unravel.talksy.main.domain.usecase

import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import praksa.unravel.talksy.main.model.Chat
import javax.inject.Inject

class FetchChatsWithUserDetailsUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    suspend operator fun invoke(userId: String): List<Chat> {
        return repository.fetchChatsWithUserDetails(userId)
    }
}
