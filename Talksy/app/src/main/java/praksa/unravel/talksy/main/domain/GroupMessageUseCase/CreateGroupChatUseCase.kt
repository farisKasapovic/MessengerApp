package praksa.unravel.talksy.main.domain.GroupMessageUseCase

import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import javax.inject.Inject

class CreateGroupChatUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    suspend operator fun invoke(groupName: String, userIds: List<String>):String {
        return repository.createGroupChat(groupName, userIds)
    }
}
