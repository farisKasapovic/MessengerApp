package praksa.unravel.talksy.main.domain.usecase

import praksa.unravel.talksy.main.data.repositories.ContactsRepository
import javax.inject.Inject

class CreateChatUseCase @Inject constructor(
    private val contactsRepository: ContactsRepository
) {
    suspend fun invoke(contactId: String): String {
        return contactsRepository.createOrFetchChat(contactId)
    }
}
