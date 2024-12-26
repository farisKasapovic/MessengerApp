package praksa.unravel.talksy.main.data.repositories


import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserStatusRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {

    private val userStatusRef: DatabaseReference
        get() = database.getReference("status")

    suspend fun setUserOnline() {
        val userId = auth.currentUser?.uid ?: return
        val statusRef = userStatusRef.child(userId)
        statusRef.setValue(
            mapOf(
                "online" to true,
                "lastSeen" to ServerValue.TIMESTAMP
            )
        ).await()
        }

    suspend fun setUserOffline() {
        val userId = auth.currentUser?.uid ?: return
        val statusRef = userStatusRef.child(userId)
        statusRef.setValue(
            mapOf(
                "online" to false,
                "lastSeen" to ServerValue.TIMESTAMP
            )
        ).await()
    }

fun getUserStatus(userId: String): Flow<Pair<Boolean, Timestamp?>> = flow {
    try {
        val statusRef = userStatusRef.child(userId)
        val snapshot = statusRef.get().await()

        val isOnline = snapshot.child("online").getValue(Boolean::class.java) ?: false

        val lastSeenMillis = snapshot.child("lastSeen").getValue(Long::class.java)
        val lastSeenTimestamp = lastSeenMillis?.let { Timestamp(it / 1000, (it % 1000).toInt() * 1000000) }

        emit(Pair(isOnline, lastSeenTimestamp))
    } catch (e: Exception) {
        Log.e("UserStatusRepository", "Error fetching user status: ${e.message}")
        emit(Pair(false, null))
    }
}




}
