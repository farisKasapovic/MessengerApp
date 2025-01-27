package praksa.unravel.talksy.main.model
import com.google.firebase.Timestamp

data class Contact(
    var username: String ="",
    var id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Timestamp? = null,
    val profilePictureUrl: String? = null
)
