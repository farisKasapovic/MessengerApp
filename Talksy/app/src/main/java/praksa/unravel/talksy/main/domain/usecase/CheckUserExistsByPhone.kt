package praksa.unravel.talksy.main.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.main.data.repositories.ContactsRepository
import javax.inject.Inject

class CheckUserExistsByPhone @Inject constructor(private val contactRepository: ContactsRepository) {
    suspend operator fun invoke(phoneNumber:String):String?{
        return contactRepository.checkUserExistsByPhone(phoneNumber)
    }
}