package praksa.unravel.talksy.main.ui.chat

import android.media.Image
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.withContext
import praksa.unravel.talksy.R
import praksa.unravel.talksy.main.model.Message
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.coroutineContext

class MessageAdapter(
    private var messages: List<Message>,
    private val currentUserId: String, // ID trenutno prijavljenog korisnika za odvajanje svojih i tuđih poruka
    private val onImageClick:(String)-> Unit,
    private val onMessageLongPress: (String) -> Unit
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val timestamp: TextView = itemView.findViewById(R.id.messageTimestamp)
        val messageImage: ImageView = itemView.findViewById(R.id.messageImage)
        val playButton: ImageView = itemView.findViewById(R.id.playButton)
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

        if (message.text.isNotEmpty()) {
            holder.messageText.visibility = View.VISIBLE
            holder.messageText.text = message.text
            holder.itemView.setOnLongClickListener{
                onMessageLongPress(message.id)
                true
            }
        } else {
            holder.messageText.visibility = View.GONE
        }

        if (!message.imageUrl.isNullOrEmpty()) {
            holder.messageImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(message.imageUrl)
                .placeholder(R.drawable.dots)
                .into(holder.messageImage)

            holder.messageImage.setOnClickListener{
                onImageClick(message.imageUrl)
            }
            holder.messageImage.setOnLongClickListener{
                onMessageLongPress(message.id)
                true
            }
        } else {
            holder.messageImage.visibility = View.GONE
        }

        // Handle voice message
        if (!message.voiceUrl.isNullOrEmpty()) {
            holder.playButton.visibility = View.VISIBLE
            holder.playButton.setOnClickListener {
                playVoiceMessage(message.voiceUrl)
            }
        } else {
            holder.playButton.visibility = View.GONE
        }

        holder.timestamp.text = SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(Date(message.timestamp))
    }

    override fun getItemCount(): Int = messages.size


    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    private fun playVoiceMessage(voiceUrl: String) {
        try {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(voiceUrl)
                prepare()
                start()
            }

            mediaPlayer.setOnCompletionListener {}
        } catch (e: Exception) {
            Log.e("MessageAdapter", "Error playing voice message: ${e.message}")
        }


    }
}
