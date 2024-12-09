package praksa.unravel.talksy.common.exception

sealed class Result <out T> {
    data class success<out T>(val data: T) : Result<T>()
    data class failure(val error: Throwable) : Result<Nothing>()
}




