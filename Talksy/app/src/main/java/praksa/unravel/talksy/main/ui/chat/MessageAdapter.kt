package praksa.unravel.talksy.main.ui.chat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import praksa.unravel.talksy.R
import praksa.unravel.talksy.main.model.Message
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private var messages: List<Message>,
    private val currentUserId: String // ID trenutno prijavljenog korisnika za odvajanje svojih i tuđih poruka
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val timestamp: TextView = itemView.findViewById(R.id.messageTimestamp)
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_SENT) {
            R.layout.item_message_sent
        } else {
            R.layout.item_message_received
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.text
        holder.timestamp.text = SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(Date(message.timestamp))
    }

    override fun getItemCount(): Int = messages.size


    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}
