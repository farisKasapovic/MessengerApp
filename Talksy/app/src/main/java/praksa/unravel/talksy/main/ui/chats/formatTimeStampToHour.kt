package praksa.unravel.talksy.main.ui.chats

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestampToHour(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault()) // Adjust the format as needed
    return formatter.format(date)
}