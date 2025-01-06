package praksa.unravel.talksy.model

import com.google.firebase.Timestamp

data class User(
    var id: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val profilePicture: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val bio: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Timestamp? = null
)

