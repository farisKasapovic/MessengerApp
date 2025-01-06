package praksa.unravel.talksy.main.domain.usecase

import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.main.data.repositories.ContactsRepository
import praksa.unravel.talksy.main.model.Contact
import javax.inject.Inject
import praksa.unravel.talksy.common.result.Result

class GetContactsUseCase @Inject constructor(private val repository: ContactsRepository) {
    operator fun invoke(): Flow<Result<List<Contact>>> {
        return repository.getContacts()
    }
}
