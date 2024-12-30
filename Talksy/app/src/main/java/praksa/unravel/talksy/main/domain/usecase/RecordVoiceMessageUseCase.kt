package praksa.unravel.talksy.main.domain.usecase

import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import javax.inject.Inject

class RecordVoiceMessageUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    suspend operator fun invoke(chatId: String, filePath: String, senderId: String) {
        val voiceUrl = repository.uploadVoiceMessage(chatId, filePath)
        if (voiceUrl != null) {
            repository.sendVoiceMessage(chatId, senderId, voiceUrl)
        } else {
            throw Exception("Failed to upload voice message")
        }
    }
}
