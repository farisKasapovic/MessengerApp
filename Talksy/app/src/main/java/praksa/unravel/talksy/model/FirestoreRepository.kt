package praksa.unravel.talksy.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import praksa.unravel.talksy.model.User

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun addUser(user: User): Boolean {
        return try {
            db.collection("users").document(user.id).set(user).await()
            true
        } catch (e: FirebaseFirestoreException) {
            false
        }
    }

    suspend fun getUsers(): List<User> {
        return try {
            val snapshot: QuerySnapshot = db.collection("users").get().await()
            snapshot.toObjects(User::class.java)
        } catch (e: FirebaseFirestoreException) {
            emptyList()
        }
    }
}
