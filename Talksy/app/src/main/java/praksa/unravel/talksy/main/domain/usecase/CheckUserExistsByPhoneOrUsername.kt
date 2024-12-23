package praksa.unravel.talksy.main.domain.usecase

import praksa.unravel.talksy.main.data.repositories.ContactsRepository
import javax.inject.Inject

class CheckUserExistsByPhoneOrUsername @Inject constructor(private val contactRepository: ContactsRepository) {
    suspend operator fun invoke(phoneNumber:String,username: String):String?{
        return contactRepository.checkUserExistsByPhoneOrUsername(phoneNumber,username)
    }
}