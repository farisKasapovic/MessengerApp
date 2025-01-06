package praksa.unravel.talksy.main.data.repositories


import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import praksa.unravel.talksy.common.asFlow
import praksa.unravel.talksy.common.mapSuccess
import praksa.unravel.talksy.main.model.Chat
import praksa.unravel.talksy.main.model.Message
import praksa.unravel.talksy.model.User
import java.io.File
import javax.inject.Inject
import praksa.unravel.talksy.common.result.Result

class DirectMessageRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    fun getUserInformation(chatId: String): Flow<Result<User?>> {
        return firestore.collection("Chats")
            .document(chatId)
            .get()
            .asFlow()
            .flatMapConcat { result ->
                when (result) {
                    is Result.Success -> {
                        val chatSnapshot = result.data
                        val users = chatSnapshot.get("users") as? List<String>
                        val contactId = users?.firstOrNull { it != auth.currentUser?.uid }

                        if (contactId != null) {
                            firestore.collection("Users")
                                .document(contactId)
                                .get()
                                .asFlow()
                                .mapSuccess { userSnapshot ->
                                    userSnapshot.toObject(User::class.java)
                                }
                        } else {
                            flowOf(Result.Success(null))
                        }
                    }
                    is Result.Failure -> flowOf(Result.Failure(result.error))
                }
            }
    }

fun fetchMessages(chatId: String): Flow<Result<List<Message>>> {
    return firestore.collection("Chats")
        .document(chatId)
        .collection("Messages")
        .orderBy("timestamp")
        .get()
        .asFlow()
        .mapSuccess { querySnapshot ->
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(Message::class.java)?.apply {
                    id = document.id
                }
            }
        }
}


    suspend fun sendMessage(chatId: String, message: Message) {
        try {
            val chatDocument = firestore.collection("Chats").document(chatId).get().await()
            val isGroup = chatDocument.getBoolean("isGroup") ?: false
            if (isGroup) {
                sendGroupMessage(chatId, message)
            } else {
                sendDirectMessage(chatId, message)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun observeMessages(
        chatId: String,
        onMessagesChanged: (List<Message>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        firestore.collection("Chats")
            .document(chatId)
            .collection("Messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val messages = snapshot.toObjects(Message::class.java)
                    onMessagesChanged(messages)
                }
            }
    }

 fun observeChats(
     userId: String,
     onChatsUpdated: (List<Chat>) -> Unit,
     onError: (Exception) -> Unit
 ) {
     firestore.collection("Chats")
         .whereArrayContains("users", userId)
         .addSnapshotListener { snapshot, error ->
             if (error != null) {
                 onError(error)
                 return@addSnapshotListener
             }

             if (snapshot != null && !snapshot.isEmpty) {
                 val chatsWithDetails = mutableListOf<Chat>()

                 snapshot.documents.forEach { document ->
                     val chat = document.toObject(Chat::class.java)?.apply {
                         id = document.id
                         isImage = document.getString("isImage")?.isNotEmpty() ?: false
                         isVoiceMessage = document.getString("isVoice")?.isNotEmpty() ?: false
                         groupName = document.getString("groupName")
                         isGroup = !groupName.isNullOrEmpty()
                     }

                     if (chat != null) {
                         val isGroup = !chat.groupName.isNullOrEmpty()

                         firestore.collection("Chats")
                             .document(chat.id)
                             .collection("Messages")
                             .orderBy(
                                 "timestamp",
                                 com.google.firebase.firestore.Query.Direction.DESCENDING
                             )
                             .limit(1)
                             .get()
                             .addOnSuccessListener { messagesSnapshot ->
                                 val lastMessageText = if (!messagesSnapshot.isEmpty) {
                                     val message = messagesSnapshot.documents.first().toObject(Message::class.java)
                                     message?.text ?: ""
                                 } else {
                                     ""
                                 }
                                 if (!messagesSnapshot.isEmpty) {
                                     if (isGroup) {
                                         chatsWithDetails.add(
                                             Chat(
                                                 id = chat.id,
                                                 isGroup = true,
                                                 groupName = chat.groupName,
                                                 lastMessage = lastMessageText,
                                                 timestamp = chat.timestamp,
                                                 users = chat.users,
                                                 isImage = chat.isImage,
                                                 isVoiceMessage = chat.isVoiceMessage
                                             )
                                         )
                                     } else {
                                         val users = document.get("users") as List<String>
                                         val contactId = users.firstOrNull { it != userId }

                                         if (contactId != null) {
                                             firestore.collection("Users").document(contactId)
                                                 .get()
                                                 .addOnSuccessListener { userSnapshot ->
                                                     val user =
                                                         userSnapshot.toObject(User::class.java)
                                                     if (user != null) {
                                                         chatsWithDetails.add(
                                                             Chat(
                                                                 id = chat.id,
                                                                 name = user.username,
                                                                 lastMessage = lastMessageText,
                                                                 timestamp = chat.timestamp,
                                                                 users = chat.users,
                                                                 isImage = chat.isImage,
                                                                 isVoiceMessage = chat.isVoiceMessage
                                                             )
                                                         )
                                                     }
                                                     onChatsUpdated(chatsWithDetails)
                                                 }
                                         }
                                     }
                                 }
                             }
                             .addOnFailureListener { e ->
                                 Log.e("DirectMessageRepository", "Error fetching messages: ${e.message}")
                             }
                     }
                 }
             }
         }
 }

    fun incrementUnreadCount(chatId: String, recipientId: String): Flow<Result<Unit>> {
        return firestore.collection("Chats")
            .document(chatId)
            .update("unreadCount.$recipientId", FieldValue.increment(1))
            .asFlow()
            .mapSuccess { Unit }
    }


    suspend fun markMessagesAsSeen(chatId: String, userId: String) {
        try {

            val messagesRef = firestore.collection("Chats").document(chatId).collection("Messages")
            // Note: Used filter here on firebase because im using multiple filters (two filters)
            val unseenMessages = messagesRef
                .whereEqualTo("seen", false)
                .whereNotEqualTo("senderId", userId)
                .get()
                .await()

            firestore.runBatch { batch ->
                for (message in unseenMessages.documents) {
                    batch.update(message.reference, "seen", true)
                }
            }

            firestore.collection("Chats").document(chatId)
                .update("unreadCount.$userId", 0)
                .await()

        } catch (e: Exception) {
            Log.e("DirectMessageRepository", "Error marking messages as seen: ${e.message}")
        }
    }

fun getUnreadCount(chatId: String, userId: String): Flow<Result<Int>> {
    return firestore.collection("Chats")
        .document(chatId)
        .get()
        .asFlow()
        .mapSuccess { chatDocument ->
            val unreadCountMap = chatDocument.get("unreadCount") as? Map<String, Long> ?: return@mapSuccess 0
            unreadCountMap[userId]?.toInt() ?: 0
        }
}

    suspend fun uploadImageAndSendMessage(chatId: String, imageUri: Uri, senderId: String) {
        try {
            val storageRef = FirebaseStorage.getInstance().reference
                .child("chats/$chatId/images/${System.currentTimeMillis()}.jpg")

            storageRef.putFile(imageUri).await()

            val imageUrl = storageRef.downloadUrl.await().toString()

            val message = Message(
                text = "",
                senderId = senderId,
                timestamp = System.currentTimeMillis(),
                seen = false,
                imageUrl = imageUrl
            )

            sendMessage(chatId, message)
        } catch (e: Exception) {
            Log.e("DirectMessageRepository", "Error uploading image: ${e.message}")
            throw e
        }
    }


    fun deleteMessage(messageId: String, chatId: String): Flow<Result<Unit>> {
        return firestore.collection("Chats")
            .document(chatId)
            .collection("Messages")
            .document(messageId)
            .delete()
            .asFlow()
            .mapSuccess { Unit }
    }

    suspend fun uploadVoiceMessage(chatId: String, filePath: String): String? {
        return try {
            val fileUri = Uri.fromFile(File(filePath))
            val storageRef = FirebaseStorage.getInstance().reference
                .child("chats/$chatId/voice_messages/${System.currentTimeMillis()}.m4a")

            storageRef.putFile(fileUri).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("DirectMessageRepository", "Error uploading voice message: ${e.message}")
            null
        }
    }

    suspend fun sendVoiceMessage(chatId: String, senderId: String, voiceUrl: String) {
        val message = Message(
            id = "",
            text = "",
            senderId = senderId,
            timestamp = System.currentTimeMillis(),
            seen = false,
            voiceUrl = voiceUrl
        )
        sendMessage(chatId, message)
    }

fun createGroupChat(groupName: String, userIds: List<String>): Flow<Result<String>> {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        ?: return flowOf(Result.Failure(IllegalStateException("User is not logged in")))

    val allUserIds = userIds.toMutableSet().apply { add(currentUserId) }

    val chatId = firestore.collection("Chats").document().id
    val chatData = hashMapOf(
        "id" to chatId,
        "users" to allUserIds.toList(),
        "isGroup" to true,
        "groupName" to groupName,
        "lastMessage" to "",
        "timestamp" to System.currentTimeMillis(),
        "unreadCount" to allUserIds.toList().associateWith { 0 }
    )

    return firestore.collection("Chats")
        .document(chatId)
        .set(chatData)
        .asFlow()
        .mapSuccess { chatId }
}



    private suspend fun sendDirectMessage(chatId: String, message: Message) {
        try {
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

            firestore.collection("Chats")
                .document(chatId)
                .update(
                    "lastMessage", message.text,
                    "timestamp", message.timestamp,
                    "isImage", message.imageUrl,
                    "isVoice", message.voiceUrl
                ).await()
            val users = firestore.collection("Chats").document(chatId).get().await()
                .get("users") as? List<String>
            val recipientId = users?.firstOrNull { it != message.senderId }

            if (recipientId != null) {
                incrementUnreadCount(chatId, recipientId)
            }
        } catch (e: Exception) {
            Log.e("DirectMessageRepository", "Error sending direct message: ${e.message}")
            throw e
        }
    }

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

    fun markGroupMessagesAsSeen(chatId: String, userId: String): Flow<Result<Unit>> {
        return firestore.collection("Chats")
            .document(chatId)
            .update("unreadCount.$userId", 0)
            .asFlow()
            .mapSuccess { Unit }
    }


    fun fetchImageMessages(chatId: String): Flow<Result<List<String>>> {
        return firestore.collection("Chats")
            .document(chatId)
            .collection("Messages")
            .whereNotEqualTo("imageUrl", null)
            .get()
            .asFlow()
            .mapSuccess { querySnapshot ->
                querySnapshot.documents.mapNotNull { it.getString("imageUrl") }
            }
    }


    fun fetchChatName(chatId: String): Flow<Result<String?>> {
        return firestore.collection("Chats")
            .document(chatId)
            .get()
            .asFlow()
            .mapSuccess { chatDocument ->
                val isGroup = chatDocument.getBoolean("isGroup") ?: false
                if (isGroup) {
                    chatDocument.getString("groupName")
                } else {
                    null
                }
            }
    }

    fun isGroupChat(chatId: String): Flow<Result<Boolean>> {
        return firestore.collection("Chats")
            .document(chatId)
            .get()
            .asFlow()
            .mapSuccess { chatDocument ->
                chatDocument.getBoolean("isGroup") ?: false
            }
    }

    fun fetchGroupMembers(chatId: String): Flow<Result<List<User>>> = flow {
        try {
            val chatSnapshot = firestore.collection("Chats").document(chatId).get().await()
            val userIds = chatSnapshot.get("users") as? List<String> ?: emptyList()

            val members = userIds.mapNotNull { userId ->
                try {
                    val userSnapshot = firestore.collection("Users").document(userId).get().await()
                    val user = userSnapshot.toObject(User::class.java)
                    user
                } catch (e: Exception) {
                    null
                }
            }
            emit(Result.Success(members))
        } catch (e: Exception) {
            emit(Result.Failure(e))
        }
    }






}
