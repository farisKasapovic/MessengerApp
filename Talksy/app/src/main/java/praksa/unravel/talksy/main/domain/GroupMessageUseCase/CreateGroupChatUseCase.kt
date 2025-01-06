package praksa.unravel.talksy.main.domain.GroupMessageUseCase

import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import javax.inject.Inject
import praksa.unravel.talksy.common.result.Result

class CreateGroupChatUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
     operator fun invoke(groupName: String, userIds: List<String>): Flow<Result<String>> {
        return repository.createGroupChat(groupName, userIds)
    }
}
