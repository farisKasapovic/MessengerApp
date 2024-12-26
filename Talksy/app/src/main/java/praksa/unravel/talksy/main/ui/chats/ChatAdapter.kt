package praksa.unravel.talksy.main.ui.chats


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import praksa.unravel.talksy.R
import praksa.unravel.talksy.main.model.Chat

class ChatAdapter(
    private var chats: List<Chat>,
    private val onChatClick: (String) -> Unit,
    private val onProfilePictureLoad: (String,ImageView) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatName: TextView = itemView.findViewById(R.id.usernameTV)
        val lastMessage: TextView = itemView.findViewById(R.id.textMessageTV)
        val profilePicture: ImageView = itemView.findViewById(R.id.pictureIV)
        val lastMessageTime: TextView = itemView.findViewById(R.id.timeTV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        Log.d("ChatAdapter","vrijednost ${chats[position].name} and ${chats[position].lastMessage}")
        val chat = chats[position]
        holder.chatName.text = chat.name // Ili ime drugog korisnika
        holder.lastMessage.text = chat.lastMessage
        onProfilePictureLoad(chat.users[1],holder.profilePicture)

        holder.lastMessageTime.text = formatTimestampToHour(chat.timestamp)


        holder.itemView.setOnClickListener {
            onChatClick(chat.id)
        }
    }

    override fun getItemCount(): Int = chats.size

    fun updateChats(newChats: List<Chat>) {
        chats = newChats
        notifyDataSetChanged()
    }
}
