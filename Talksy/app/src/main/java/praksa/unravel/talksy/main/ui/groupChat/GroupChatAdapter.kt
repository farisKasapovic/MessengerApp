package praksa.unravel.talksy.main.ui.groupChat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import praksa.unravel.talksy.R
import praksa.unravel.talksy.main.model.Contact
import praksa.unravel.talksy.main.ui.contacts.formatLastSeen

class GroupChatAdapter(
    private val items: List<Contact>,
    private val preselectedUserId: String, // Preselected user ID
    private val onContactSelected: (String, Boolean) -> Unit
) : RecyclerView.Adapter<GroupChatAdapter.GroupChatViewHolder>() {
    private val selectedContacts = mutableSetOf<String>().apply { add(preselectedUserId) }

    inner class GroupChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fullNameTV: TextView = itemView.findViewById(R.id.contactFullNameTV)
        val profilePictureIV: ImageView = itemView.findViewById(R.id.contactImageIV)
        val selectCircleIV: ImageView = itemView.findViewById(R.id.selectCircleIV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_chat_contact, parent, false)
        return GroupChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupChatViewHolder, position: Int) {
        val contact = items[position]
        val isSelected = selectedContacts.contains(contact.id)

        val fullName = "${contact.firstName} ${contact.lastName}"
        holder.fullNameTV.text = fullName
        Glide.with(holder.itemView.context)
            .load(contact.profilePictureUrl)
            .placeholder(R.drawable.default_profile_picture)
            .into(holder.profilePictureIV)

        holder.selectCircleIV.setImageResource(
            if (isSelected) R.drawable.circle_selected else R.drawable.circle_unselected
        )

        if (contact.id == preselectedUserId) {
            holder.selectCircleIV.alpha = 0.5f
            holder.itemView.setOnClickListener(null)
            Log.d("GroupChatAdapter", "Preselected user: ${contact.id} is always selected.")
        } else {
            holder.selectCircleIV.alpha = 1.0f
            holder.itemView.setOnClickListener {
                if (isSelected) {
                    selectedContacts.remove(contact.id)
                } else {
                    selectedContacts.add(contact.id)
                }
                onContactSelected(contact.id, !isSelected)
                notifyItemChanged(position)
            }
        }

    }

    override fun getItemCount(): Int = items.size
}







