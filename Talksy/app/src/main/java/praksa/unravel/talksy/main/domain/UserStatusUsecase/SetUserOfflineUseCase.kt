package praksa.unravel.talksy.main.domain.UserStatusUsecase

import praksa.unravel.talksy.main.data.repositories.UserStatusRepository
import javax.inject.Inject

class SetUserOfflineUseCase @Inject constructor(
    private val repository: UserStatusRepository
) {
    suspend operator fun invoke() {
        repository.setUserOffline()
    }
}