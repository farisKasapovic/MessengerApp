package praksa.unravel.talksy.main.ui.contacts


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import praksa.unravel.talksy.main.domain.UserStatusUsecase.GetUserStatusUseCase
import praksa.unravel.talksy.main.domain.UserStatusUsecase.SetUserOfflineUseCase
import praksa.unravel.talksy.main.domain.UserStatusUsecase.SetUserOnlineUseCase
import javax.inject.Inject

@HiltViewModel
class UserStatusViewModel @Inject constructor(
    private val setUserOnlineUseCase: SetUserOnlineUseCase,
    private val setUserOfflineUseCase: SetUserOfflineUseCase,
    private val getUserStatusUseCase: GetUserStatusUseCase
) : ViewModel() {

    private val _status = MutableStateFlow<Pair<String, Pair<Boolean, Timestamp?>>?>(null)
    val status: StateFlow<Pair<String, Pair<Boolean, Timestamp?>>?> = _status

    fun setUserOnline() {
        viewModelScope.launch {
            setUserOnlineUseCase()
        }
    }

    fun setUserOffline() {
        viewModelScope.launch {
            setUserOfflineUseCase()
        }
    }

    fun fetchUserStatus(userId: String) {
        viewModelScope.launch {
            getUserStatusUseCase(userId).collect { userStatus ->
                _status.value = Pair(userId, userStatus)
            }
        }
    }
}
