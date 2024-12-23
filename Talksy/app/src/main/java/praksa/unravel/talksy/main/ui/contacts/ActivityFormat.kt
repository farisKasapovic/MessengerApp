package praksa.unravel.talksy.main.ui.contacts

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

fun formatLastSeen(lastSeen: Timestamp?): String {
    if (lastSeen == null) return "Offline"

    val now = System.currentTimeMillis()
    val lastSeenMillis = lastSeen.toDate().time
    val diff = now - lastSeenMillis

    return when {
        diff < 60 * 1000 -> "Just now" // Less than a minute
        diff < 60 * 60 * 1000 -> {
            val minutes = diff / (60 * 1000)
            "$minutes minute${if (minutes > 1) "s" else ""} ago"
        }
        diff < 24 * 60 * 60 * 1000 -> {
            val hours = diff / (60 * 60 * 1000)
            "$hours hour${if (hours > 1) "s" else ""} ago"
        }
        diff < 30 * 24 * 60 * 60 * 1000 -> {
            val days = diff / (24 * 60 * 60 * 1000)
            "$days day${if (days > 1) "s" else ""} ago"
        }
        diff < 365 * 24 * 60 * 60 * 1000 -> {
            val months = diff / (30 * 24 * 60 * 60 * 1000)
            if (months == 0L) {
                val days = diff / (24 * 60 * 60 * 1000)
                "$days day${if (days > 1) "s" else ""} ago"
            } else {
                "$months month${if (months > 1) "s" else ""} ago"
            }
        }
        else -> {
            val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(lastSeen.toDate())
            "Last seen on $date"
        }
    }
}
