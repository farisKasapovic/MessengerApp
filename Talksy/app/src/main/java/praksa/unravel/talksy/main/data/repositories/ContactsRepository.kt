package praksa.unravel.talksy.main.data.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import praksa.unravel.talksy.common.asFlow
import praksa.unravel.talksy.common.mapSuccess
import praksa.unravel.talksy.main.model.Contact
import praksa.unravel.talksy.common.result.Result

class ContactsRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private val userId
        get() = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

fun addContact(contact: Contact, addedUserId: String): Flow<Result<Unit>> {
    return db.collection("Users")
        .document(userId)
        .collection("contacts")
        .document(addedUserId)
        .set(contact)
        .asFlow()
        .mapSuccess { Unit }
}


    fun checkUserExistsByPhoneOrUsername(phoneNumber: String, username: String): Flow<Result<String?>> = flow {
        val phoneQuery = db.collection("Users")
            .whereEqualTo("phone", phoneNumber)
            .get()
            .await()

        if (!phoneQuery.isEmpty) {
            emit(Result.Success(phoneQuery.documents[0].id))
            return@flow
        }

        emitAll(
            db.collection("Users")
                .whereEqualTo("username", username)
                .get()
                .asFlow()
                .mapSuccess { usernameQuery ->
                    if (!usernameQuery.isEmpty) usernameQuery.documents[0].id else null
                }
        )
    }

fun getContacts(): Flow<Result<List<Contact>>> {
    return db.collection("Users")
        .document(userId)
        .collection("contacts")
        .get()
        .asFlow()
        .mapSuccess { snapshot -> snapshot.toObjects(Contact::class.java) }
}

fun getProfilePictureUrl(userId: String): Flow<Result<String?>> {
    Log.d("Checking", "Ovaj userId $userId")
    val path = "profile_pictures/$userId"
    Log.d("Checking", "Ovaj path $path")
    val storageRef = FirebaseStorage.getInstance().reference.child(path)
    return storageRef.downloadUrl.asFlow()
        .mapSuccess { url ->
            Log.d("Checking", "Download successful: $url")
            url.toString()
        }
        .catch { error ->
            Log.e("Checking", "Error fetching download URL: ${error.message}", error)
            emit(Result.Failure(error))
        }
}

fun createOrFetchChat(contactId: String): Flow<Result<String>> {
    return flow {
        val existingChat = db.collection("Chats")
            .whereArrayContains("users", userId)
            .get()
            .await()
            .documents.firstOrNull { document ->
                val users = document.get("users") as? List<*>
                users?.contains(contactId) == true
            }

        if (existingChat != null) {
            emitAll(
                db.collection("Chats")
                    .document(existingChat.id)
                    .collection("Messages")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .asFlow()
                    .mapSuccess { messagesSnapshot ->
                        val lastMessageText = messagesSnapshot.documents
                            .firstOrNull()
                            ?.getString("text") ?: "No messages yet"
                        existingChat.id
                    }
            )
        } else {
            val newChat = hashMapOf(
                "users" to listOf(userId, contactId),
                "lastMessage" to "",
                "timestamp" to System.currentTimeMillis()
            )
            emitAll(
                db.collection("Chats")
                    .add(newChat)
                    .asFlow()
                    .mapSuccess { it.id }
            )
        }
    }
  }
}

