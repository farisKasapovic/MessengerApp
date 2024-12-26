package praksa.unravel.talksy.main.ui.chats

import android.util.Log
import kotlinx.coroutines.tasks.await
import praksa.unravel.talksy.main.model.Chat

class ChatsState {

  /*  suspend fun fetchChatsWithMessages(userId: String): List<Chat> {
        Log.d("DMrepo","vrijednost userida $userId")

        return try {
            val snapshot = firestore.collection("Chats")
                .whereArrayContains("users", userId)
                .get()
                .await()
            Log.d("DMrepo","vrijednost je ${snapshot.documents}")


            val chatsWithMessages = mutableListOf<Chat>()

            for (document in snapshot.documents) {
                val chat = document.toObject(Chat::class.java)?.apply { id = document.id }
                if (chat != null) {

                    val messagesSnapshot = firestore.collection("Chats")
                        .document(chat.id)
                        .collection("Messages")
                        .limit(1)
                        .get()
                        .await()

                    if (!messagesSnapshot.isEmpty) {
                        chatsWithMessages.add(chat)
                    }
                }
            }
            Log.d("DirectMessageRepository", "Fetched chats with messages: $chatsWithMessages")
            chatsWithMessages
        } catch (e: Exception) {
            Log.e("DirectMessageRepository", "Error fetching chats: ${e.message}")
            emptyList()
        }
    }*/

}