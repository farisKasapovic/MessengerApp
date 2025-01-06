package praksa.unravel.talksy.main.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.model.User
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {

    fun getCurrentUser(): Flow<Result<User>> = flow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            emit(Result.Failure(Throwable("User not logged in")))
            return@flow
        }

        try {
            val userSnapshot = firestore.collection("Users").document(currentUserId).get().await()
            val user = userSnapshot.toObject(User::class.java)
            if (user != null) {
                emit(Result.Success(user))
            } else {
                emit(Result.Failure(Throwable("User not found")))
            }
        } catch (e: Exception) {
            emit(Result.Failure(e))
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.Failure(Throwable("User not logged in"))

            firestore.collection("Users").document(currentUserId).set(user).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}
