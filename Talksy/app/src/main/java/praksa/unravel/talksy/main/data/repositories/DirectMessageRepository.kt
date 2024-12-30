package praksa.unravel.talksy.main.data.repositories


import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import praksa.unravel.talksy.main.model.Chat
import praksa.unravel.talksy.main.model.Message
import praksa.unravel.talksy.model.User
import java.io.File
import javax.inject.Inject

class DirectMessageRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {


    // Fetch the second user's details from the chat document
    suspend fun getUserInformation(chatId: String): User? {
        return try {
            val chatDocument = firestore.collection("Chats").document(chatId).get().await()
            val users = chatDocument.get("users") as? List<String>

            if (users != null) {
                val contactId = users.firstOrNull { it != auth.currentUser?.uid }
                if (contactId != null) {
                    val userDocument =
                        firestore.collection("Users").document(contactId).get().await()
                    userDocument.toObject(User::class.java)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    //Fetching messages
//    suspend fun fetchMessages(chatId: String): List<Message> {
//        return try {
//            firestore.collection("Chats")
//                .document(chatId)
//                .collection("Messages")
//                .orderBy("timestamp")
//                .get()
//                .await()
//                .toObjects(Message::class.java)
//        } catch (e: Exception) {
//            emptyList()
//        }
//    }
    suspend fun fetchMessages(chatId: String): List<Message> {
        return try {
            val querySnapshot = firestore.collection("Chats")
                .document(chatId)
                .collection("Messages")
                .orderBy("timestamp")
                .get()
                .await()

            querySnapshot.documents.map { document ->
                val message = document.toObject(Message::class.java)
                message?.apply {
                    id = document.id // Set the Firestore document ID
                }
            }.filterNotNull()
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun sendMessage(chatId: String, message: Message) {
        try {
            val messageRef = firestore.collection("Chats")
                .document(chatId)
                .collection("Messages")
                .add(message)
                .await()

            val messageId = messageRef.id

            //Zbog brisanja poruka
            firestore.collection("Chats")
                .document(chatId)
                .collection("Messages")
                .document(messageId)
                .update("id", messageId)
                .await()

            firestore.collection("Chats")
                .document(chatId)
                .update("lastMessage",message.text,
                    "timestamp",message.timestamp)
                .await()

            val users = firestore.collection("Chats").document(chatId).get().await()
                .get("users") as? List<String>
            val recipientId = users?.firstOrNull { it != message.senderId }

            if (recipientId != null) {
                incrementUnreadCount(chatId, recipientId)
            }


        } catch (e: Exception) {
            Log.e("DirectMessageRepository", "Error sending message: ${e.message}")
            throw e
        }
    }


    //unchecked

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
                    Log.e("DirectMessageRepository", "Error observing messages: ${error.message}")
                    onError(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val messages = snapshot.toObjects(Message::class.java)
                    onMessagesChanged(messages)
                }
            }
    }

    suspend fun fetchChatsWithUserDetails(userId: String): List<Chat> {
        val chatsWithDetails = mutableListOf<Chat>()

        try {
            val snapshot = firestore.collection("Chats")
                .whereArrayContains("users", userId)
                .get()
                .await()

            for (document in snapshot.documents) {
                val chat = document.toObject(Chat::class.java)?.apply { id = document.id }
                if (chat != null) {
                    val users = document.get("users") as List<String>
                    Log.d("dmrepo", "Users in chat: $users")

                    // Pronađi ID drugog korisnika
                    val contactId = users.firstOrNull { it != userId }
                    Log.d("dmrepo", "Contact ID: $contactId")

                    if (contactId != null) {
                        // Provjeri da li chat ima poruke
                        val messagesSnapshot = firestore.collection("Chats")
                            .document(chat.id)
                            .collection("Messages")
                            .orderBy(
                                "timestamp",
                                com.google.firebase.firestore.Query.Direction.DESCENDING
                            )
                            .limit(1)
                            .get()
                            .await()

                        val lastMessageText = if (!messagesSnapshot.isEmpty) {
                            val message =
                                messagesSnapshot.documents.first().toObject(Message::class.java)
                            message?.text ?: ""
                        } else {
                            ""
                        }


                        if (!messagesSnapshot.isEmpty) {
                            // Ako postoje poruke, dohvatite korisnika
                            val userSnapshot =
                                firestore.collection("Users").document(contactId).get().await()
                            val user = userSnapshot.toObject(User::class.java)

                            if (user != null) {
                                // Dodajte chat samo ako postoji međusobna komunikacija
                                chatsWithDetails.add(
                                    Chat(
                                        id = chat.id,
                                        name = user.username,
                                        lastMessage = lastMessageText,
                                        timestamp = chat.timestamp,
                                        users = chat.users
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DirectMessageRepository", "Error fetching chats with user details: ${e.message}")
        }

        return chatsWithDetails
    }

 //ORGINAL VERZIJA
    fun observeChats(
        userId: String,
        onChatsUpdated: (List<Chat>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        firestore.collection("Chats")
            .whereArrayContains("users", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DirectMessageRepository", "Error observing chats: ${error.message}")
                    onError(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val chatsWithDetails = mutableListOf<Chat>()

                    snapshot.documents.forEach { document ->
                        val chat = document.toObject(Chat::class.java)?.apply { id = document.id }
                        Log.d("ovo ","ovo mi trenutno treba ${chat?.id}")
                        if (chat != null) {
                            val users = document.get("users") as List<String>
                            val contactId = users.firstOrNull { it != userId }

                            if (contactId != null) {
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
                                            val message = messagesSnapshot.documents.first()
                                                .toObject(Message::class.java)
                                            message?.text ?: ""
                                        } else {
                                            ""
                                        }

                                        if (!messagesSnapshot.isEmpty) {
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
                                                                users = chat.users
                                                            )
                                                        )

                                                        onChatsUpdated(chatsWithDetails) // Emitujte ažurirane podatke
                                                    }
                                                }
                                        }
                                    }
                            }
                        }
                    }
                }
            }
    }


    private suspend fun incrementUnreadCount(chatId: String, recipientId: String) {
        firestore.collection("Chats").document(chatId)
            .update("unreadCount.$recipientId", FieldValue.increment(1))
            .await()
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

    suspend fun getUnreadCount(chatId: String, userId: String): Int {
        val chatDocument = firestore.collection("Chats").document(chatId).get().await()
        val unreadCountMap = chatDocument.get("unreadCount") as? Map<String, Long> ?: return 0
        return unreadCountMap[userId]?.toInt() ?: 0
    }

    suspend fun uploadImageAndSendMessage(chatId: String, imageUri: Uri, senderId: String) {
        try {
            val storageRef = FirebaseStorage.getInstance().reference
                .child("chats/$chatId/images/${System.currentTimeMillis()}.jpg")

            val uploadTask = storageRef.putFile(imageUri).await()

            val imageUrl = storageRef.downloadUrl.await().toString()

            val message = Message(
                text = "",
                senderId = senderId,
                timestamp = System.currentTimeMillis(),
                seen = false,
                imageUrl = imageUrl
            )

            sendMessage(chatId, message) // Koristi postojeću metodu za slanje poruke
        } catch (e: Exception) {
            Log.e("DirectMessageRepository", "Error uploading image: ${e.message}")
            throw e
        }
    }


    suspend fun deleteMessage(messageId: String,chatId: String) {
        try {
            firestore.collection("Chats")
                .document(chatId)
                .collection("Messages")
                .document(messageId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("DirectMessageRepository", "Error deleting message: ${e.message}")
            throw e
        }
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












}
