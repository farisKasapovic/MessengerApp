package praksa.unravel.talksy.main.domain.usecase

import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import praksa.unravel.talksy.model.User
import javax.inject.Inject

class GetUserInformationUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    suspend operator fun invoke(chatId: String): User? {
        return repository.getUserInformation(chatId)
    }
}