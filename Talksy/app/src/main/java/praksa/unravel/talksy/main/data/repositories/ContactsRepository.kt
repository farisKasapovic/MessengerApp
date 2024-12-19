package praksa.unravel.talksy.main.data.repositories

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import praksa.unravel.talksy.main.model.Contact

class ContactsRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
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

    suspend fun checkUserExistsByPhone(phoneNumber: String): String? {
        val querySnapshot = db.collection("Users")
            .whereEqualTo("phone", phoneNumber)
            .get()
            .await()

        return if (!querySnapshot.isEmpty) {
            querySnapshot.documents[0].id   //vratiti userID
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
}

