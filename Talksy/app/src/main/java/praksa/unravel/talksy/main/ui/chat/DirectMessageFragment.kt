package praksa.unravel.talksy.main.ui.chat

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import praksa.unravel.talksy.main.data.services.NotificationManagerService
import praksa.unravel.talksy.main.model.Message
import praksa.unravel.talksy.main.ui.contacts.formatLastSeen
import praksa.unravel.talksy.model.User
import praksa.unravel.talksy.utils.ToastUtils
import praksa.unravel.talksy.utils.VoiceMessageHandler
import praksa.unravel.talksy.common.result.Result


@AndroidEntryPoint
class DirectMessageFragment : Fragment() {

    private lateinit var binding: FragmentDirectMessageBinding
    private val viewModel: DirectMessageViewModel by viewModels()
    private val currentUserId: String by lazy {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var voiceMessageHandler: VoiceMessageHandler
    private var isRecording = false
    private lateinit var recordedFilePath: String



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

        binding.sendIV.setOnClickListener {
            val text = binding.messageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.sendMessage(chatId, text)
                binding.messageInput.text.clear()
                binding.messageInput.clearFocus();
            }

            binding.sendIV.visibility  = View.GONE
            binding.pickImageIV.visibility = View.VISIBLE
            binding.microphoneIV.visibility = View.VISIBLE
        }

        binding.backIV.setOnClickListener {
            findNavController().popBackStack()
        }


        setupRecyclerView()
        observeViewModel(chatId)
        viewModel.observeMessages(chatId)

        lifecycleScope.launchWhenStarted {
            viewModel.markMessagesAsSeen(chatId)
        }


        binding.pickImageIV.setOnClickListener{
            openGallery()
        }

        voiceMessageHandler = VoiceMessageHandler(
            context = requireContext(),
            onDurationUpdate = { duration ->
                updateRecordingUI(duration)
            },
            onPlaybackUpdate = { playbackDuration ->
                updatePlaybackUI(playbackDuration)
            }
        )


        binding.microphoneIV.setOnClickListener {
            if (checkAndRequestPermissions()) {
                if (!isRecording) {
                    startVoiceRecording()
                } else {
                    stopVoiceRecordingAndSend()
                }
            } else {
                Toast.makeText(requireContext(), "Microphone permission is required to record audio.", Toast.LENGTH_SHORT).show()
            }
        }



        val notificationService = NotificationManagerService(requireContext())
        notificationService.listenForNewMessages(chatId)


        binding.dotsIV.setOnClickListener {
            viewModel.checkIfGroupChat(chatId)

            viewModel.directMessageState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is DirectMessageState.GroupCheckSuccess -> {
                        if (state.isGroup) {
                            val action = DirectMessageFragmentDirections.actionDirectMessageFragmentToGroupChatInfoFragment(chatId)
                            findNavController().navigate(action)
                        } else {
                            val action = DirectMessageFragmentDirections.actionDirectMessageFragmentToProfileFragment(chatId)
                            findNavController().navigate(action)
                        }
                    }
                    is DirectMessageState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Ignore other states
                    }
                }
            }
        }







        //
        binding.messageInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    toggleIconsForMessageInput(isEditing = false)
                } else {
                    toggleIconsForMessageInput(isEditing = true)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun startVoiceRecording() {
        isRecording = true
        recordedFilePath = voiceMessageHandler.startRecording()
        Log.d("Notify","uslo unutar startVoiceRecording")

        // Show the recording UI
        binding.recordingUI.visibility = View.VISIBLE
        binding.recordDurationTextView.text = "0s"
        binding.recordLoader.progress = 0

        Toast.makeText(requireContext(), "Recording started...", Toast.LENGTH_SHORT).show()
    }


    private fun stopVoiceRecordingAndSend() {
        isRecording = false
        voiceMessageHandler.stopRecording()

        // Hide the recording UI
        binding.recordingUI.visibility = View.GONE // Ensure this is executed

        val chatId = arguments?.getString("chatId") ?: return
        viewModel.sendVoiceMessage(chatId, recordedFilePath)
        Toast.makeText(requireContext(), "Recording finished.", Toast.LENGTH_SHORT).show()
    }


    private fun updateRecordingUI(duration: Int) {
        binding.recordDurationTextView.text = "${duration}s"
        binding.recordLoader.progress = duration
    }




    private fun updatePlaybackUI(playbackDuration: Int) {
        Log.d("DirectMessageFragment", "Playback progress updated: $playbackDuration seconds")

        binding.recordDurationTextView.text = "$playbackDuration s"
        binding.recordLoader.progress = playbackDuration
    }



    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(
            messages = emptyList(),
            currentUserId = currentUserId,
            onImageClick = { imageUrl ->
                openImage(imageUrl)
            },
            onMessageLongPress = { messageId ->
                showDeleteConfirmation(messageId)
            }
        )

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
                    Log.d("Trenutno","trenutno mi treba ovo je li uslo u userSucces ${state.user.username}")
                    showUserDetails(state.user,chatId)
                    viewModel.fetchUserStatus(state.user.id)

                }

                is DirectMessageState.MessagesSuccess -> updateMessages(state.messages)
                is DirectMessageState.Error -> Toast.makeText(
                    context,
                    state.message,
                    Toast.LENGTH_SHORT
                ).show()

                is DirectMessageState.UserStatus -> updateUserStatus(chatId,state.pair)
                is DirectMessageState.ChatsSuccess -> {}//updateChats(state.chats)
                is DirectMessageState.GroupCheckSuccess -> {}
            }
        }
    }


    private fun showUserDetails(user: User, chatId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            Log.d("Trenutno", "Trenutno mi treba ovo USLI SMO U FUN $user ")
            val groupName = viewModel.getChatName(chatId)

            Log.d("Trenutno", "Trenutno mi treba ovo $user")

            if (!groupName.isNullOrEmpty()) {
                binding.profileNameTV.text = groupName
                binding.profileImageIV.setImageResource(R.drawable.ic_group)
            } else {
                if (user != null) {
                    binding.profileNameTV.text = user.username

                    viewModel.getProfilePictureUrl(user.id).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                val profilePictureUrl = result.data
                                Log.d("ovdje", "ppUrl $profilePictureUrl")
                                Glide.with(requireContext())
                                    .load(profilePictureUrl)
                                    .placeholder(R.drawable.default_profile_picture)
                                    .into(binding.profileImageIV)
                            }
                            is Result.Failure -> {
                                Log.e("ovdje", "Error fetching profile picture: ${result.error.message}")
                                binding.profileImageIV.setImageResource(R.drawable.default_profile_picture)
                            }
                        }
                    }
                }
            }
        }
    }



    private fun updateMessages(messages: List<Message>) {
        messageAdapter.updateMessages(messages)
        if (messages.isNotEmpty()) {
            binding.messagesRV.scrollToPosition(messages.size - 1) // Last message
        }
        val chatId = arguments?.getString("chatId") ?: return
        viewModel.markMessagesAsSeen(chatId)
    }

    private fun updateUserStatus(chatId: String,status: Pair<String, Pair<Boolean, Timestamp?>>) {
        viewLifecycleOwner.lifecycleScope.launch {
            val groupName = viewModel.getChatName(chatId)
        val isOnline = status.second.first
        val lastSeen = status.second.second
            if (!groupName.isNullOrEmpty()) {
                binding.activityStatusTV.text = ""
    } else {
                binding.activityStatusTV.text = if (isOnline) {
                    "Online"
                } else {
                    formatLastSeen(lastSeen)
                }
    }
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val chatId = arguments?.getString("chatId") ?: return@registerForActivityResult
            viewModel.uploadImageAndSendMessage(chatId, uri)
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }
    private fun openImage(imageUrl: String) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_image_fullscreen)
        val imageView = dialog.findViewById<ImageView>(R.id.fullscreenImageView)

        Glide.with(requireContext())
            .load(imageUrl)
            .placeholder(R.drawable.dots)
            .into(imageView)
        dialog.show()
    }
    private fun showDeleteConfirmation(messageId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Message")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton("Delete") { _, _ ->
                deleteMessage(messageId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMessage(messageId: String) {
        val chatId = arguments?.getString("chatId") ?: return
        viewModel.deleteMessage(messageId,chatId)
        context?.let { ToastUtils.showCustomToast(it.applicationContext,"Message deleted") }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permission granted", Toast.LENGTH_SHORT).show()
                startVoiceRecording() // Pokreni snimanje ako je dozvola data
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun checkAndRequestPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSION_REQUEST_CODE
            )
            return false
        }
        return true
    }

    private fun toggleIconsForMessageInput(isEditing: Boolean) {
        if (isEditing) {

            binding.pickImageIV.visibility = View.GONE
            binding.microphoneIV.visibility = View.GONE
            binding.sendIV.visibility = View.VISIBLE
        } else {

            binding.pickImageIV.visibility = View.VISIBLE
            binding.microphoneIV.visibility = View.VISIBLE
            binding.sendIV.visibility = View.GONE
        }
    }

    private companion object {
        const val PERMISSION_REQUEST_CODE = 101
    }

}


