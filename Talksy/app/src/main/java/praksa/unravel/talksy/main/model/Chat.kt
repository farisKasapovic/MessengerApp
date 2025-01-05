package praksa.unravel.talksy.main.model

data class Chat(
    var id: String = "",
    val name: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L,
    val users: List<String> = emptyList(),
    var isImage: Boolean = false,
    var isVoiceMessage: Boolean = false,
    var isGroup: Boolean = false,
    var groupName: String? = null
)
