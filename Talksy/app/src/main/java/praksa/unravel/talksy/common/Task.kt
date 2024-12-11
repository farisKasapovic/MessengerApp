package praksa.unravel.talksy.common

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow

fun <T>Task<T>.asFlow() = flow {
    val sharedFlow: MutableSharedFlow<T> = MutableSharedFlow()
    this@asFlow.addOnSuccessListener { sharedFlow.tryEmit(it) }
    this@asFlow.addOnFailureListener { error(it.message.orEmpty()) }
    sharedFlow.collectLatest { emit(it) }
}


/*žž
*
 suspend fun checkEmailExists(email: String): Result<Boolean> =
        db.collection("Users")
            .whereEqualTo("email", email)
            .get()
            .asFlow()
            .map { !it.isEmpty }
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
            .first()

    suspend fun checkUsernameExists(username: String): Result<Boolean> =
        db.collection("Users")
            .whereEqualTo("username", username)
            .get()
            .asFlow()
            .map { !it.isEmpty }
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
            .first()
*
* */