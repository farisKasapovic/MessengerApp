sealed class CodeState {
    object Idle : CodeState()
    object Loading : CodeState()
    data class Success(val message: String) : CodeState()
    data class Error(val message: String) : CodeState()
}
