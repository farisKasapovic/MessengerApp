package praksa.unravel.talksy.main.domain.usecase

import praksa.unravel.talksy.main.data.repositories.ContactsRepository
import javax.inject.Inject

class GetProfilePictureUrlUseCase @Inject constructor(private val repository: ContactsRepository) {
    suspend operator fun invoke(userId: String):String?{
        return repository.getProfilePictureUrl(userId)
    }
}