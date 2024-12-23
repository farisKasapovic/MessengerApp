package praksa.unravel.talksy.main.model
import com.google.firebase.Timestamp

data class Contact(
    var id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val isOnline: Boolean = false, // Tracks online status
    val lastSeen: Timestamp? = null // Stores last seen timestamp
)