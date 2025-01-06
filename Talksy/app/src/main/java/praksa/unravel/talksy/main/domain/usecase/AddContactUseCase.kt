package praksa.unravel.talksy.main.domain.usecase

import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.main.data.repositories.ContactsRepository
import praksa.unravel.talksy.main.model.Contact
import javax.inject.Inject
import praksa.unravel.talksy.common.result.Result

class AddContactUseCase @Inject constructor(private val repository: ContactsRepository) {
    operator fun invoke(contact: Contact, addedUserId: String): Flow<Result<Unit>> {
        return repository.addContact(contact, addedUserId)
    }
}
