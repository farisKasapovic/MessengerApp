package praksa.unravel.talksy.main.ui.contacts
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import praksa.unravel.talksy.R
import praksa.unravel.talksy.databinding.ChatsFragmentBinding
import praksa.unravel.talksy.databinding.ContactsFragmentBinding
import praksa.unravel.talksy.main.model.Contact
import praksa.unravel.talksy.ui.contacts.ContactsAdapter
import praksa.unravel.talksy.common.result.Result

@AndroidEntryPoint
class ContactsFragment : Fragment() {

    private lateinit var binding: ContactsFragmentBinding
    private val viewModel: ContactsViewModel by viewModels()
    private val activityStatusViewModel: UserStatusViewModel by viewModels()
    private lateinit var adapter: ContactsAdapter
    private val itemsList = mutableListOf<Any>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = ContactsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addIcon = binding.addContactIV
        addIcon.setOnClickListener { toggleMenuItems() }

        binding.contactRecyclerView.layoutManager =LinearLayoutManager(requireContext())

        adapter = ContactsAdapter(itemsList, { userId, imageView ->
            viewLifecycleOwner.lifecycleScope.launch {
                val profilePictureUrl = viewModel.getProfilePictureUrl(userId)
                Glide.with(requireContext())
                    .load(profilePictureUrl)
                    .placeholder(R.drawable.default_profile_picture)
                    .into(imageView)
            }
        }, { menuType ->
            handleMenuItemClick(menuType)
        }, { contact ->
            startChat(contact)
        })

        binding.contactRecyclerView.adapter = adapter

        observeViewModel()
        observeUserStatuses()


        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result

            Log.d("vidi","rezultat $token")

        })

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

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    is ContactsState.Loading -> {}
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
                    adapter.notifyItemChanged(contactIndex)
                }
            }
        }
    }

    private fun fetchUserStatuses(contacts: List<Contact>) {
        contacts.forEach { contact ->
            activityStatusViewModel.fetchUserStatus(contact.id)
        }
    }

    private fun startChat(contact: Contact) {
        lifecycleScope.launch {
            viewModel.createOrFetchChat(contact.id)
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            navigateToChatsFragment(result.data)
                        }
                        is Result.Failure -> {
                            Toast.makeText(requireContext(), "Failed to start chat: ${result.error.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }

    private fun navigateToChatsFragment(chatId: String) {
       val action = ContactsFragmentDirections.actionContactsFragmentToDirectMessageFragment(chatId)
        findNavController().navigate(action)
    }

}
