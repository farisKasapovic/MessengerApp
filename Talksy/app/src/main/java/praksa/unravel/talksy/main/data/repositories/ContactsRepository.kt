package praksa.unravel.talksy.main.data.repositories

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.main.model.Contact
import praksa.unravel.talksy.model.User
import com.google.firebase.Timestamp
import com.google.firebase.database.FirebaseDatabase

class ContactsRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
) {

    private val userId
        get() = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")



    suspend fun addContact(contact: Contact, addedUserId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.d("ContactsRepository", "User is not logged in!")
            throw IllegalStateException("User not logged in")
        }
        Log.d("ContactRepo", "vrijednost $userId")

        try {
            FirebaseFirestore.getInstance()
                .collection("Users") // Glavna kolekcija korisnika
                .document(userId) // Dokument korisnika
                .collection("contacts") // Podkolekcija kontakata
                .document(addedUserId)
                .set(contact)
                //.add(contact) // Dodavanje kontakta
                .addOnSuccessListener { Log.d("Firestore", "Contact added successfully!") }
                .addOnFailureListener { e -> Log.e("Firestore", "Error adding contact", e) }
                .await()
        } catch (e: Exception) {
            Log.e("ContactsRepository", "Error: ${e.message}")
            throw e
        }
    }

    //Check if user exists in our database by phoneNumber, if not null

    suspend fun checkUserExistsByPhoneOrUsername(phoneNumber: String, username: String): String? {
        val phoneQuery = db.collection("Users")
            .whereEqualTo("phone", phoneNumber)
            .get()
            .await()

        if (!phoneQuery.isEmpty) {
            return phoneQuery.documents[0].id // Vraća ID korisnika pronađenog po broju
        }

        val usernameQuery = db.collection("Users")
            .whereEqualTo("username", username)
            .get()
            .await()

        return if (!usernameQuery.isEmpty) {
            usernameQuery.documents[0].id // Vraća ID korisnika pronađenog po korisničkom imenu
        } else {
            null // Nema rezultata
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
// NOVOO

    // Added: Update user's online/offline status
//    fun updateUserStatus(isOnline: Boolean) {
//        val userId = auth.currentUser?.uid ?: return
//        val updates = if (isOnline) {
//            mapOf("isOnline" to true)
//        } else {
//            mapOf(
//                "isOnline" to false,
//                "lastSeen" to Timestamp.now()
//            )
//        }
//
//            db.collection("Users").document(userId)
//            .update(updates)
//            .addOnSuccessListener { println("User status updated successfully.") }
//            .addOnFailureListener { println("Error updating user status: ${it.message}") }
//    }
//
//    fun getUserStatus(userId: String): Flow<Result<User>> = flow {
//        try {
//            val documentSnapshot = db.collection("Users").document(userId).get().await()
//            val user = documentSnapshot.toObject(User::class.java)
//            if (user != null) {
//                emit(Result.Success(user))
//            } else {
//                emit(Result.Failure(Exception("User not found")))
//            }
//        } catch (e: Exception) {
//            emit(Result.Failure(e))
//        }
//    }

}

