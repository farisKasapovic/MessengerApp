package praksa.unravel.talksy.main.data.repositories


import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import praksa.unravel.talksy.main.model.Chat
import praksa.unravel.talksy.main.model.Message
import praksa.unravel.talksy.model.User
import javax.inject.Inject

class DirectMessageRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

//    suspend fun fetchUserDetails(userId: String): User? {
//        return try {
//            val document = firestore.collection("Users").document(userId).get().await()
//            document.toObject(User::class.java)
//        } catch (e: Exception) {
//            null
//        }
//    }

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
    suspend fun fetchMessages(chatId: String): List<Message> {
        return try {
            firestore.collection("Chats")
                .document(chatId)
                .collection("Messages")
                .orderBy("timestamp")
                .get()
                .await()
                .toObjects(Message::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun sendMessage(chatId: String, message: Message) {
        try {
            firestore.collection("Chats")
                .document(chatId)
                .collection("Messages")
                .add(message)
                .await()

            firestore.collection("Chats")
                .document(chatId)
                .update("lastMessage",message.text,
                    "timestamp",message.timestamp)
                .await()


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


    fun najnoviji(
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
}
