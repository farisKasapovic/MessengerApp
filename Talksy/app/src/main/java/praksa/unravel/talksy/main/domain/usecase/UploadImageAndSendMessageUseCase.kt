package praksa.unravel.talksy.main.domain.usecase

import android.net.Uri
import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import javax.inject.Inject

class UploadImageAndSendMessageUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    suspend operator fun invoke(chatId: String, imageUri: Uri, senderId: String) {
        repository.uploadImageAndSendMessage(chatId, imageUri, senderId)
    }
}
