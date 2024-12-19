package praksa.unravel.talksy.main.ui.newcontact


sealed class NewContactState {
    object Loading : NewContactState()
    object Success : NewContactState()
    data class Error(val message: String) : NewContactState()
}
