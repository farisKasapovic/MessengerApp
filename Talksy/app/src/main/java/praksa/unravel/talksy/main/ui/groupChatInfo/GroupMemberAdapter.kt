package praksa.unravel.talksy.main.ui.groupChatInfo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import praksa.unravel.talksy.R
import praksa.unravel.talksy.main.ui.contacts.formatLastSeen
import praksa.unravel.talksy.model.User
class GroupMemberAdapter(
    private var members: List<User>
) : RecyclerView.Adapter<GroupMemberAdapter.MemberViewHolder>() {

    private var profilePictures: Map<String, String?> = emptyMap()

    fun updateMembers(newMembers: List<User>) {
        members = newMembers
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.bind(member, profilePictures[member.id])
    }

    override fun getItemCount(): Int = members.size

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pictureIV: ImageView = itemView.findViewById(R.id.pictureIV)
        private val firstNameTV: TextView = itemView.findViewById(R.id.firstNameTV)
        private val lastNameTV: TextView = itemView.findViewById(R.id.lastNameTV)
        private val activityTV: TextView = itemView.findViewById(R.id.activityTV)

        fun bind(user: User, profilePictureUrl: String?) {
            firstNameTV.text = user.username
            lastNameTV.visibility = View.GONE
            activityTV.text = "${user.firstName} ${user.lastName}"

            Glide.with(itemView.context)
                .load(profilePictureUrl ?: R.drawable.default_profile_picture)
                .into(pictureIV)
        }
    }
}



