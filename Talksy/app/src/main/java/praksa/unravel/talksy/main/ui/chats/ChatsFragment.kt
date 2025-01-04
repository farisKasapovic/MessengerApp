package praksa.unravel.talksy.main.ui.chats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import praksa.unravel.talksy.R
import praksa.unravel.talksy.databinding.ChatsFragmentBinding


@AndroidEntryPoint
class ChatsFragment : Fragment() {

    private lateinit var binding: ChatsFragmentBinding
    private val viewModel: ChatsViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChatsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        setupRecyclerView(userId)
        observeViewModel()
        viewModel.observeChats(userId)
    }

    private fun setupRecyclerView(userUid:String) {
        chatAdapter = ChatAdapter(
            chats = emptyList(),
            onChatClick = { chatId ->
                navigateToDirectMessage(chatId)
            },
            onProfilePictureLoad = { userId, imageView ->
                loadProfilePicture(userId, imageView)
            },
            onUnreadCountFetch = { chatId, onUnreadCountReceived ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val unreadCount = viewModel.getUnreadCount(chatId, userUid)
                    onUnreadCountReceived(unreadCount)
                }
            }
        )
        binding.chatRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }
    }

    private fun navigateToDirectMessage(chatId: String) {
        val action = ChatsFragmentDirections.actionChatsFragmentToDirectMessageFragment(chatId)
        findNavController().navigate(action)
    }

    private fun observeViewModel() {
        viewModel.chatsState.observe(viewLifecycleOwner) { chats ->
            if (chats.isEmpty()) {
                Toast.makeText(requireContext(), "No chats available", Toast.LENGTH_SHORT).show()
            } else {
                val sortedChats = chats.sortedByDescending { it.timestamp }
                chatAdapter.updateChats(sortedChats)
            }
        }
    }

    private fun loadProfilePicture(userId: String, imageView: ImageView) {
        viewLifecycleOwner.lifecycleScope.launch {
            val profilePictureUrl = viewModel.getProfilePictureUrl(userId)
            Glide.with(requireContext())
                .load(profilePictureUrl)
                .placeholder(R.drawable.default_profile_picture)
                .into(imageView)
        }
    }
}



