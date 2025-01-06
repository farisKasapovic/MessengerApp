package praksa.unravel.talksy.main.ui.profile

import android.os.Bundle
import android.provider.ContactsContract.Profile
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import praksa.unravel.talksy.R
import praksa.unravel.talksy.databinding.ProfileFragmentBinding
import praksa.unravel.talksy.main.ui.chat.DirectMessageState
import praksa.unravel.talksy.main.ui.chat.DirectMessageViewModel
import praksa.unravel.talksy.main.ui.contacts.formatLastSeen
import praksa.unravel.talksy.model.User
import praksa.unravel.talksy.common.result.Result

@AndroidEntryPoint
class ProfileFragment: Fragment() {

    private lateinit var binding: ProfileFragmentBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ProfileFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var chatId = arguments?.getString("chatId") ?: ""
        Log.d("Check","uslo u profileFragment $chatId")

        binding.backIV.setOnClickListener{
            findNavController().popBackStack()
        }

       observeViewModel(chatId)

        binding.createGroupChatTV.setOnClickListener {
            val userId = viewModel.profileState.value?.let { state ->
                when (state) {
                    is ProfileState.UserSuccess -> state.user.id
                    is ProfileState.UserStatus -> state.pair.first
                    else -> null
                }
            }

            if (userId != null) {
                val action = ProfileFragmentDirections.actionProfileFragmentToGroupChatFragment(userId)
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Failed to get user ID", Toast.LENGTH_SHORT).show()
            }
        }



    }
    private fun observeViewModel(chatId: String) {
        viewModel.getUserInformation(chatId)
        viewModel.fetchImageMessages(chatId)

        viewModel.profileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileState.Error -> Toast.makeText(context,state.message,Toast.LENGTH_SHORT).show()
                is ProfileState.Loading -> {Log.d("s","check")}
                is ProfileState.UserStatus -> {
                    updateUserStatus(state.pair)

                }
                is ProfileState.UserSuccess -> { showUserDetails(state.user)
                    viewModel.fetchUserStatus(state.user.id)}
                is ProfileState.ImagesSuccess -> setupRecyclerView(state.imageUrls)
            }
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
    private fun showUserDetails(user: User) {
        binding.profileNameTV.text = user.firstName
        binding.myBioTV.text = user.bio
        binding.myUsernameTV.text = user.username

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getProfilePictureUrl(user.id).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val profilePictureUrl = result.data
                        Glide.with(requireContext())
                            .load(profilePictureUrl)
                            .placeholder(R.drawable.default_profile_picture)
                            .into(binding.profileImageIV)
                    }
                    is Result.Failure -> {
                        Log.e("Profile", "Error loading profile picture: ${result.error.message}")
                        binding.profileImageIV.setImageResource(R.drawable.default_profile_picture)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView(images: List<String>) {
        val adapter = ImageAdapter(images)
        binding.galleryRV.apply {
            layoutManager = GridLayoutManager(requireContext(), 3) // 3 slike u redu
            this.adapter = adapter
        }
    }



}