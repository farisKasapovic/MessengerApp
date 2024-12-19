package praksa.unravel.talksy.main.domain.usecase

import praksa.unravel.talksy.main.data.repositories.ContactsRepository
import praksa.unravel.talksy.main.model.Contact
import javax.inject.Inject

class AddContactUseCase @Inject constructor(private val repository: ContactsRepository) {
    suspend operator fun invoke(contact: Contact,addedUserId:String) = repository.addContact(contact,addedUserId)
}