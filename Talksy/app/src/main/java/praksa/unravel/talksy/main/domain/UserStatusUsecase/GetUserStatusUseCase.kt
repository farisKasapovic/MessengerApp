package praksa.unravel.talksy.main.domain.UserStatusUsecase

import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.main.data.repositories.UserStatusRepository
import javax.inject.Inject

class GetUserStatusUseCase @Inject constructor(
    private val repository: UserStatusRepository
) {
    operator fun invoke(userId: String): Flow<Pair<Boolean, Timestamp?>> {
        return repository.getUserStatus(userId)
    }
}