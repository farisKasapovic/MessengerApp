package praksa.unravel.talksy.main.ui.groupChat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.main.domain.GroupMessageUseCase.CreateGroupChatUseCase
import praksa.unravel.talksy.main.domain.UserStatusUsecase.GetUserStatusUseCase
import praksa.unravel.talksy.main.domain.usecase.GetContactsUseCase
import praksa.unravel.talksy.main.domain.usecase.GetProfilePictureUrlUseCase
import praksa.unravel.talksy.main.model.Contact
import javax.inject.Inject

@HiltViewModel
class GroupChatViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase,
    private val getProfilePictureUrlUseCase: GetProfilePictureUrlUseCase,
    private val getUserStatusUseCase: GetUserStatusUseCase,
    private val createGroupChatUseCase: CreateGroupChatUseCase
) : ViewModel() {

    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> = _contacts

    private val _selectedUserIds = MutableLiveData<Set<String>>(emptySet())
    val selectedUserIds: LiveData<Set<String>> = _selectedUserIds

    fun fetchContacts() {
        viewModelScope.launch {
            getContactsUseCase()
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val contactList = result.data
                            val enrichedContacts = contactList.map { contact ->
                                val profilePictureUrlResult = getProfilePictureUrlUseCase(contact.id)
                                    .first()
                                when (profilePictureUrlResult) {
                                    is Result.Success -> contact.copy(profilePictureUrl = profilePictureUrlResult.data)
                                    is Result.Failure -> {
                                        Log.e("GroupChatViewModel", "Error fetching profile picture: ${profilePictureUrlResult.error}")
                                        contact
                                    }
                                }
                            }
                            _contacts.value = enrichedContacts
                        }
                        is Result.Failure -> {
                            Log.e("GroupChatViewModel", "Error fetching contacts: ${result.error}")
                        }
                    }
                }
        }
    }



    fun toggleContactSelection(userId: String, isSelected: Boolean) {
        val currentSelection = _selectedUserIds.value.orEmpty().toMutableSet()
        if (isSelected) currentSelection.add(userId) else currentSelection.remove(userId)
        _selectedUserIds.value = currentSelection
    }

fun createGroupChat(groupName: String, preselectedUser: String, onGroupChatCreated: (String) -> Unit) {
    viewModelScope.launch {
        val userIds = _selectedUserIds.value.orEmpty().toMutableSet().apply {
            add(preselectedUser)
        }.toList()

        if (userIds.isEmpty()) {
            Log.e("GroupChatViewModel", "No users selected!")
            return@launch
        }

        createGroupChatUseCase(groupName, userIds).collect { result ->
            when (result) {
                is Result.Success -> {
                    val chatId = result.data
                    onGroupChatCreated(chatId)
                }
                is Result.Failure -> {
                    Log.e("GroupChatViewModel", "Error creating group chat: ${result.error.message}")
                }
            }
        }
    }
}





}
