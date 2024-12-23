package praksa.unravel.talksy.main.ui.contacts
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import praksa.unravel.talksy.R
import praksa.unravel.talksy.main.model.Contact
import praksa.unravel.talksy.ui.contacts.ContactsAdapter


@AndroidEntryPoint
class ContactsFragment : Fragment() {

    private val viewModel: ContactsViewModel by viewModels()
    private val activityStatusViewModel: UserStatusViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactsAdapter
    private val itemsList = mutableListOf<Any>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.contacts_fragment, container, false)
        val addIcon = view.findViewById<View>(R.id.addContactIV)

        addIcon.setOnClickListener { toggleMenuItems() }

        recyclerView = view.findViewById(R.id.contactRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ContactsAdapter(itemsList, { userId, imageView ->
            viewLifecycleOwner.lifecycleScope.launch {
                val profilePictureUrl = viewModel.getProfilePictureUrl(userId)
                Glide.with(requireContext())
                    .load(profilePictureUrl)
                    .placeholder(R.drawable.default_profile_picture)
                    .into(imageView)
            }
        }) { menuPosition ->
            handleMenuItemClick(menuPosition)

        }
        recyclerView.adapter = adapter

        observeViewModel()
        observeUserStatuses()
        return view
    }

    private fun toggleMenuItems() {
        val isMenuPresent = itemsList.any { it is MenuType }

        if (isMenuPresent) {
            itemsList.removeAll { it is MenuType }
            adapter.notifyDataSetChanged()
        } else {
            itemsList.add(0, MenuType.FIND_PEOPLE)
            itemsList.add(1, MenuType.INVITE_PEOPLE)
            itemsList.add(2, MenuType.NEW_CONTACT)
            adapter.notifyItemRangeInserted(0, 3)
        }
    }



    private fun handleMenuItemClick(menuType: MenuType) {
        when (menuType) {
            MenuType.FIND_PEOPLE -> {
                Toast.makeText(requireContext(), "Find People clicked", Toast.LENGTH_SHORT).show()
            }
            MenuType.INVITE_PEOPLE -> {
                Toast.makeText(requireContext(), "Invite People clicked", Toast.LENGTH_SHORT).show()
            }
            MenuType.NEW_CONTACT -> {
                findNavController().navigate(R.id.action_contactsFragment_to_newContactFragment)
            }
        }
    }




// Standardno
    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    is ContactsState.Loading -> {
                        // Prikaz loading indikatora
                    }
                    is ContactsState.Success -> {
                        itemsList.clear()
                        itemsList.addAll(state.contacts)
                        fetchUserStatuses(state.contacts)
                        adapter.notifyDataSetChanged()
                    }
                    is ContactsState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    is ContactsState.Empty -> {
                        Toast.makeText(requireContext(), "Nema kontakata", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun observeUserStatuses() {
        lifecycleScope.launchWhenStarted {
            activityStatusViewModel.status.collect { status ->
                val (userId, userStatus) = status ?: return@collect

                // Find the corresponding contact and update its status
                val contactIndex = itemsList.indexOfFirst { it is Contact && it.id == userId }
                Log.d("ContactsFragment","vrijednost: $contactIndex")
                if (contactIndex != -1) {
                    val contact = itemsList[contactIndex] as Contact
                    val updatedContact = contact.copy(
                        isOnline = userStatus.first,
                        lastSeen = userStatus.second
                    )
                    Log.d("ContactsFragment","vrijednost: ${updatedContact.isOnline} and ${updatedContact.lastSeen}")
                    itemsList[contactIndex] = updatedContact
                    adapter.notifyItemChanged(contactIndex) // Notify adapter of the change
                }
            }
        }
    }

    private fun fetchUserStatuses(contacts: List<Contact>) {
        contacts.forEach { contact ->
            activityStatusViewModel.fetchUserStatus(contact.id) // Fetch status for each user
        }
    }



    override fun onStart() {
        super.onStart()
        activityStatusViewModel.setUserOnline()
        Log.d("ContactsFragment","vrijednost: uslo je u onStart()")
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch {
            activityStatusViewModel.setUserOffline()
        }
    }


}
