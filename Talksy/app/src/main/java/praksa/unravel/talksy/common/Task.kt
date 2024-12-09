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