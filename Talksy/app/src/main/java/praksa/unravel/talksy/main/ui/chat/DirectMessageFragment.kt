package praksa.unravel.talksy.main.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import praksa.unravel.talksy.R
import praksa.unravel.talksy.databinding.FragmentDirectMessageBinding
import praksa.unravel.talksy.main.model.Message
import praksa.unravel.talksy.main.ui.contacts.formatLastSeen
import praksa.unravel.talksy.model.User


@AndroidEntryPoint
class DirectMessageFragment : Fragment() {

    private lateinit var binding: FragmentDirectMessageBinding
    private val viewModel: DirectMessageViewModel by viewModels()
    private val currentUserId: String by lazy {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
    private lateinit var messageAdapter: MessageAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDirectMessageBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var chatId = arguments?.getString("chatId") ?: ""

        binding.sendButton.setOnClickListener {
            val text = binding.messageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.sendMessage(chatId, text)
                binding.messageInput.text.clear()
            }
        }

        binding.backIV.setOnClickListener {
            findNavController().popBackStack()
        }


        setupRecyclerView()
        observeViewModel(chatId)
        viewModel.observeMessages(chatId)
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(emptyList(), currentUserId)
        binding.messagesRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = messageAdapter
        }
    }


    private fun observeViewModel(chatId: String) {
        viewModel.fetchMessages(chatId)
        viewModel.getUserInformation(chatId)

        viewModel.directMessageState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DirectMessageState.Loading -> {}  //showLoading()
                is DirectMessageState.UserSuccess -> {
                    showUserDetails(state.user)
                    viewModel.fetchUserStatus(state.user.id)
                }

                is DirectMessageState.MessagesSuccess -> updateMessages(state.messages)
                is DirectMessageState.Error -> Toast.makeText(
                    context,
                    state.message,
                    Toast.LENGTH_SHORT
                ).show()

                is DirectMessageState.UserStatus -> updateUserStatus(state.pair)
            }
        }
    }


    private fun showUserDetails(user: User) {
        binding.profileNameTV.text = user.username
        viewLifecycleOwner.lifecycleScope.launch {

            val profilePictureUrl = viewModel.getProfilePictureUrl(user.id)
            Glide.with(requireContext())
                .load(profilePictureUrl)
                .placeholder(R.drawable.default_profile_picture)
                .into(binding.profileImageIV)
        }
    }

    private fun updateMessages(messages: List<Message>) {
        messageAdapter.updateMessages(messages)
        if (messages.isNotEmpty()) {
            binding.messagesRV.scrollToPosition(messages.size - 1) // Last message
        }
    }

    private fun updateUserStatus(status: Pair<String, Pair<Boolean, Timestamp?>>) {
        val isOnline = status.second.first
        val lastSeen = status.second.second
        binding.activityStatusTV.text = if (isOnline) {
            "Online"
        } else {
            formatLastSeen(lastSeen)
        }
    }


}


