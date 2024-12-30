package praksa.unravel.talksy.main.model

data class Message(
    var id: String ="",
    val text: String = "",
    val senderId: String = "",
    val timestamp: Long = 0,
    val seen: Boolean = false,
    val imageUrl: String? = null,
    val voiceUrl: String? = null
)

