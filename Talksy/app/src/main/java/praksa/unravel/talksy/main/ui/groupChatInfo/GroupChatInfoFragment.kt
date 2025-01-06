package praksa.unravel.talksy.main.ui.groupChatInfo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import praksa.unravel.talksy.databinding.FragmentGroupChatInfoBinding

@AndroidEntryPoint
class GroupChatInfoFragment : Fragment() {

    private lateinit var binding: FragmentGroupChatInfoBinding
    private val viewModel: GroupChatViewModel by viewModels()
    private lateinit var groupMemberAdapter: GroupMemberAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupChatInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chatId = arguments?.getString("chatId") ?: return
        Log.d("GroupChatInfoFragment", "Chat ID: $chatId")

        setupRecyclerView()
        observeViewModel()

        viewModel.fetchGroupMembers(chatId)

        binding.backIV.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        groupMemberAdapter = GroupMemberAdapter(emptyList())
        binding.membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupMemberAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is GroupChatState.MembersLoaded -> groupMemberAdapter.updateMembers(state.members)
                else -> Unit
            }
        }

    }
}
