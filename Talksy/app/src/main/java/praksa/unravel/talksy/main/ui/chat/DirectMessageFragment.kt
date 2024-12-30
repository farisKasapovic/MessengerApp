package praksa.unravel.talksy.main.ui.chat

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
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
import java.io.File
import java.io.IOException


@AndroidEntryPoint
class DirectMessageFragment : Fragment() {

    private lateinit var binding: FragmentDirectMessageBinding
    private val viewModel: DirectMessageViewModel by viewModels()
    private val currentUserId: String by lazy {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
    private lateinit var notificationService: NotificationManagerService
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var voiceMessageHandler: VoiceMessageHandler
    private var isRecording = false
    private lateinit var recordedFilePath: String
    private var isPlaying = false
    private var currentlyPlayingFile: String? = null



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


        binding.recordButton.setOnClickListener {
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



    }

    private fun startVoiceRecording() {
        isRecording = true
        recordedFilePath = voiceMessageHandler.startRecording()
        Log.d("Notify","uslo unutar startVoiceRecording")

        // Show the recording UI
        binding.recordingUI.visibility = View.VISIBLE // Ensure this is executed
        binding.recordDurationTextView.text = "0s" // Reset the duration text
        binding.recordLoader.progress = 0 // Reset the loader progress

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
        binding.recordDurationTextView.text = "${duration}s" // Update duration in seconds
        binding.recordLoader.progress = duration // Update loader progress
    }




    private fun updatePlaybackUI(playbackDuration: Int) {
        Log.d("DirectMessageFragment", "Playback progress updated: $playbackDuration seconds")

        binding.recordDurationTextView.text = "$playbackDuration s" // Update duration
        binding.recordLoader.progress = playbackDuration // Update progress bar
    }


    private fun stopPlayback() {
        isPlaying = false
        currentlyPlayingFile = null
        voiceMessageHandler.stopPlayback()

        // Reset playback UI
        binding.recordingUI.visibility = View.GONE
        binding.recordDurationTextView.text = "0s"
        binding.recordLoader.progress = 0
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
                is DirectMessageState.ChatsSuccess -> {}//updateChats(state.chats)
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
        val chatId = arguments?.getString("chatId") ?: return
        viewModel.markMessagesAsSeen(chatId)
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



    override fun onDestroyView() {
        super.onDestroyView()

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

    private companion object {
        const val PERMISSION_REQUEST_CODE = 101
    }





}


