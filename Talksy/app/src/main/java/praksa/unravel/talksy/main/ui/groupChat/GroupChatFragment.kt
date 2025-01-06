package praksa.unravel.talksy.main.ui.groupChat

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
import dagger.hilt.android.AndroidEntryPoint
import praksa.unravel.talksy.databinding.FragmentGroupChatBinding
import praksa.unravel.talksy.main.model.Contact

@AndroidEntryPoint
class GroupChatFragment : Fragment() {

    private lateinit var binding: FragmentGroupChatBinding
    private val viewModel: GroupChatViewModel by viewModels()
    private lateinit var adapter: GroupChatAdapter
    private var preselectedUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preselectedUserId = arguments?.getString("userId")
        Log.d("userid"," alooo $preselectedUserId")



        setupRecyclerView()
        observeViewModel()

        binding.createGroupChatButton.setOnClickListener {
            val groupName = binding.groupNameET.text.toString().trim()
            if (groupName.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a group name", Toast.LENGTH_SHORT).show()
            } else {
                preselectedUserId?.let { preselectedUserId ->
                    viewModel.createGroupChat(groupName, preselectedUserId) { chatId ->
                        val action = GroupChatFragmentDirections.actionGroupChatFragmentToDirectMessageFragment(chatId, groupName)
                        findNavController().navigate(action)
                    }
                }
            }
        }
        viewModel.fetchContacts()
    }

    private fun setupRecyclerView() {
        preselectedUserId?.let { userId ->
            binding.contactsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            adapter = GroupChatAdapter(emptyList(), userId) { contactId, isSelected ->
                viewModel.toggleContactSelection(contactId, isSelected)
            }
            binding.contactsRecyclerView.adapter = adapter
        } ?: run {
            Toast.makeText(requireContext(), "No preselected user ID provided", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.contacts.observe(viewLifecycleOwner) { contacts ->
            preselectedUserId?.let { userId ->
                adapter = GroupChatAdapter(contacts, userId) { contactId, isSelected ->
                    viewModel.toggleContactSelection(contactId, isSelected)
                }
                binding.contactsRecyclerView.adapter = adapter
            }
        }

    }
}



