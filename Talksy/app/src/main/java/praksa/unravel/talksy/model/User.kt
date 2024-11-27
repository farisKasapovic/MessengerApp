package praksa.unravel.talksy.model

data class User(
    val id: String = "", // Jedinstveni ID
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val firstName: String = "",
    val lastName:String = "",
    val profilePictureUrl: String = "",
    val bio: String = "",
)

