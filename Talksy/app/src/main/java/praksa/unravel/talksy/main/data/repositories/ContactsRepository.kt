package praksa.unravel.talksy.main.data.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import praksa.unravel.talksy.main.model.Contact

class ContactsRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private val userId
        get() = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

    suspend fun addContact(contact: Contact, addedUserId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            throw IllegalStateException("User not logged in")
        }
        try {
            FirebaseFirestore.getInstance()
                .collection("Users")
                .document(userId)
                .collection("contacts")
                .document(addedUserId)
                .set(contact)
                .addOnSuccessListener { Log.d("Firestore", "Contact added successfully!") }
                .addOnFailureListener { e -> Log.e("Firestore", "Error adding contact", e) }
                .await()
        } catch (e: Exception) {
            Log.e("ContactsRepository", "Error: ${e.message}")
            throw e
        }
    }

    //Check if user exists in our database by phoneNumber, if not null + mail
    suspend fun checkUserExistsByPhoneOrUsername(phoneNumber: String, username: String): String? {
        val phoneQuery = db.collection("Users")
            .whereEqualTo("phone", phoneNumber)
            .get()
            .await()

        if (!phoneQuery.isEmpty) {
            return phoneQuery.documents[0].id
        }

        val usernameQuery = db.collection("Users")
            .whereEqualTo("username", username)
            .get()
            .await()

        return if (!usernameQuery.isEmpty) {
            usernameQuery.documents[0].id
        } else {
            null
        }
    }

    suspend fun getContacts(): List<Contact> {
        val snapshot =
            db.collection("Users")
                .document(userId)
                .collection("contacts")
                .get()
                .await()
        return snapshot.toObjects(Contact::class.java)
    }

    suspend fun getProfilePictureUrl(userId: String): String? {
        return try {
            val path = "profile_pictures/$userId"
            val storageRef = FirebaseStorage.getInstance().reference.child(path)
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Log.d("FirebaseStorage", "Dohvaćen URL: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Greška pri dohvatu URL-a: ${e.message}", e)
            null
        }
    }

    suspend fun createOrFetchChat(contactId: String): String {
        val existingChat =
            db.collection("Chats")
            .whereArrayContains("users", userId) // Current User exists in any array field of a document
            .get()
            .await()
            .documents.firstOrNull { document ->
                val users = document.get("users") as List<*>
                users.contains(contactId)
            }
        return if (existingChat != null) {
            val lastMessageText = db.collection("Chats")
                .document(existingChat.id)
                .collection("Messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.getString("text") ?: "No messages yet" // Default text if no messages exist

//            //Last message (small problem...)
//            db.collection("Chats")
//                .document(existingChat.id)
//                .update("lastMessage",lastMessageText)
//                .await()

            existingChat.id
        } else {
            val newChat = hashMapOf(
                "users" to listOf(userId, contactId),
                "lastMessage" to "",
                "timestamp" to System.currentTimeMillis()
            )
            val chatRef = db.collection("Chats").add(newChat).await()
            chatRef.id
        }
    }

}

