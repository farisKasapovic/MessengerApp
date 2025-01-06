package praksa.unravel.talksy.main.domain.usecase

import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.main.data.repositories.ContactsRepository
import javax.inject.Inject
import praksa.unravel.talksy.common.result.Result

class GetProfilePictureUrlUseCase @Inject constructor(private val repository: ContactsRepository) {
    operator fun invoke(userId: String): Flow<Result<String?>> {
        return repository.getProfilePictureUrl(userId)
    }
}
