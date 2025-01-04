package praksa.unravel.talksy.main.model
import com.google.firebase.Timestamp

data class Contact(
    var id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Timestamp? = null,
    val profilePictureUrl: String? = null // New field for profile picture URL
)

/*
*
package praksa.unravel.talksy.main.ui.groupChat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import praksa.unravel.talksy.R
import praksa.unravel.talksy.main.model.Contact

@AndroidEntryPoint
class GroupChatFragment : Fragment() {

    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var createGroupChatButton: Button
    private lateinit var adapter: GroupChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_group_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views using findViewById
        contactsRecyclerView = view.findViewById(R.id.contactsRecyclerView)
        createGroupChatButton = view.findViewById(R.id.createGroupChatButton)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val mockContacts = listOf(
            Contact(id = "1", firstName = "John", lastName = "Doe"),
            Contact(id = "2", firstName = "Jane", lastName = "Smith")
        )
        Log.d("GroupChatFragment", "Observed contacts: $mockContacts")

        // Initialize adapter with mock data
        adapter = GroupChatAdapter(mockContacts)
        contactsRecyclerView.adapter = adapter

        Log.d("GroupChatFragment", "RecyclerView adapter set. Item count: ${adapter.itemCount}")
    }
}

*
*
*
*
*
*
* inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fullNameTV: TextView = itemView.findViewById(R.id.contactFullNameTV)
        val usernameTV: TextView = itemView.findViewById(R.id.contactUsernameTV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_chat_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = items[position]
        Log.d("Adapter", "Observed contacts: ${contact.firstName}")
        holder.fullNameTV.text = contact.firstName
        holder.usernameTV.text = contact.lastName
    }

    override fun getItemCount(): Int = items.siz
*
* */