package praksa.unravel.talksy.main.domain.usecase


import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import praksa.unravel.talksy.main.model.Message
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    suspend operator fun invoke(chatId: String, message: Message) {
        repository.sendMessage(chatId, message)
    }
}

/*

suspend fun sendGroupMessage(chatId: String, message: Message) {
        val messageRef = firestore.collection("Chats")
            .document(chatId)
            .collection("Messages")
            .add(message)
            .await()

        val messageId = messageRef.id

        firestore.collection("Chats")
            .document(chatId)
            .collection("Messages")
            .document(messageId)
            .update("id", messageId)
            .await()

        // Ažuriraj poslednju poruku i povećaj brojač za sve korisnike osim pošiljaoca
        val chatDocument = firestore.collection("Chats").document(chatId).get().await()
        val userIds = chatDocument.get("users") as List<String>

        firestore.collection("Chats")
            .document(chatId)
            .update(
                "lastMessage", message.text,
                "timestamp", message.timestamp,
                "isImage", message.imageUrl,
                "isVoice", message.voiceUrl
            ).await()

        val updates = userIds.filter { it != message.senderId }.associateWith { FieldValue.increment(1) }
        updates.forEach { (userId, increment) ->
            firestore.collection("Chats").document(chatId).update("unreadCount.$userId", increment).await()
        }
    }

 */
