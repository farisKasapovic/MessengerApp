package praksa.unravel.talksy.main.domain.usecase

import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import praksa.unravel.talksy.model.User
import javax.inject.Inject

class FetchGroupMembersUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    operator fun invoke(chatId: String): Flow<Result<List<User>>> {
        return repository.fetchGroupMembers(chatId)
    }
}
