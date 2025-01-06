package praksa.unravel.talksy.main.ui.groupChatInfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.main.domain.usecase.FetchGroupMembersUseCase
import praksa.unravel.talksy.main.domain.usecase.GetProfilePictureUrlUseCase
import javax.inject.Inject

@HiltViewModel
class GroupChatViewModel @Inject constructor(
    private val fetchGroupMembersUseCase: FetchGroupMembersUseCase,
    private val getProfilePictureUrlUseCase: GetProfilePictureUrlUseCase
) : ViewModel() {

    private val _state = MutableLiveData<GroupChatState>()
    val state: LiveData<GroupChatState> = _state

    fun fetchGroupMembers(chatId: String) {
        _state.value = GroupChatState.Loading
        viewModelScope.launch {
            fetchGroupMembersUseCase(chatId).collect { result ->
                when (result) {
                    is Result.Success -> _state.postValue(GroupChatState.MembersLoaded(result.data))
                    is Result.Failure -> _state.postValue(GroupChatState.Error(result.error.message ?: "Unknown Error"))
                }
            }
        }
    }
    fun getProfilePictureUrl(userId: String): Flow<Result<String?>> {
        return getProfilePictureUrlUseCase(userId)
    }



}
