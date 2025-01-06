package praksa.unravel.talksy.common

import android.util.Log
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.mapLatest
import praksa.unravel.talksy.common.result.Result

fun <T> Task<T>.asFlow() = callbackFlow<Result<T>> {

    this@asFlow.addOnSuccessListener { result ->
        val sendResult = trySend(Result.Success(result))
        if (sendResult.isFailure) {
            trySend(Result.Failure(sendResult.exceptionOrNull() ?: Throwable("Unknown error")))
        }
    }

    this@asFlow.addOnFailureListener { exception ->
        trySend(Result.Failure(exception))
    }

    awaitClose { /* leave empty */ }
}

fun <T, R> Flow<Result<T>>.mapSuccess(success: (data: T) -> R): Flow<Result<R>> {
    Log.d("Task",success.toString())
    return this.mapLatest { data ->
        when(data) {
            is Result.Failure -> Result.Failure(data.error)
            is Result.Success -> Result.Success(success(data.data))
        }
    }
}

fun <T, R: Throwable> Flow<Result<out T>>.mapError(errorData: (data: Throwable) -> R): Flow<Result<T>> {
    return this.mapLatest { data ->
        when(data) {
            is Result.Failure -> Result.Failure(errorData(data.error))
            is Result.Success -> Result.Success(data.data)
        }
    }
}




