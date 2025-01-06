package praksa.unravel.talksy.ui.auth

import CodeState
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import praksa.unravel.talksy.domain.usecase.AddUserToDatabaseUseCase
import praksa.unravel.talksy.domain.usecase.LinkPhoneNumberUseCase
import praksa.unravel.talksy.domain.usecase.RegisterUserInAuthUseCase
import praksa.unravel.talksy.domain.usecase.VerifyPhoneNumberWithCodeUseCase
import praksa.unravel.talksy.domain.usecases.DeleteUserFromAuthUseCase
import praksa.unravel.talksy.model.User
import praksa.unravel.talksy.common.result.Result
import javax.inject.Inject
@HiltViewModel
class CodeViewModel @Inject constructor(
    private val verifyPhoneNumberWithCodeUseCase: VerifyPhoneNumberWithCodeUseCase,
    private val addUserToDatabaseUseCase: AddUserToDatabaseUseCase,
    private val registerUserInAuthUseCase: RegisterUserInAuthUseCase,
    private val linkPhoneNumberUseCase: LinkPhoneNumberUseCase,
    private val deleteUserUseCase: DeleteUserFromAuthUseCase
) : ViewModel() {

    private val _codeState = MutableStateFlow<CodeState>(CodeState.Idle)
    val codeState: StateFlow<CodeState> = _codeState


    fun verifyCodeAndRegister(
        code: String,
        verificationId: String?,
        email: String,
        password: String,
        username: String,
        phoneNumber: String
    ) {
        if (verificationId.isNullOrEmpty() || code.length != 6) {
            Log.d("CodeViewModel", "Uslo je u if uslove $verificationId i $code")
            _codeState.value = CodeState.Error("Invalid verification code")
            return
        }

        viewModelScope.launch {
            try {
                _codeState.value = CodeState.Loading


                val credentialResult = verifyPhoneNumberWithCodeUseCase(verificationId, code)
                credentialResult.collectLatest { credential ->
                    when (credential) {
                        is Result.Success -> {
                            registerUserInAuthUseCase(email, password).collectLatest { userResult ->
                                when (userResult) {
                                    is Result.Success -> {
                                        val userId = userResult.data
                                        Log.d("CodeViewModel","Vrijednost userId = $userId")
                                        linkPhoneNumberUseCase(credential.data).collectLatest { phoneLinkResult ->
                                            when (phoneLinkResult) {
                                                is Result.Success -> {
                                                    val user = User(
                                                        id= userId,
                                                        email = email,
                                                        username = username,
                                                        phone = phoneNumber,
                                                        profilePicture = ""
                                                    )
                                                    addUserToDatabaseUseCase(user).collectLatest { dbResult ->
                                                        when (dbResult) {
                                                            is Result.Success -> {
                                                                _codeState.value =
                                                                    CodeState.Success("Phone number verified and user registered")
                                                            }

                                                            is Result.Failure -> {
                                                                _codeState.value =
                                                                    CodeState.Error("Failed to add user to database")
                                                                deleteUserUseCase.invoke()
                                                                return@collectLatest
                                                            }
                                                        }
                                                    }
                                                }

                                                is Result.Failure -> {
                                                    _codeState.value =
                                                        CodeState.Error("Phone verification failed")
                                                    deleteUserUseCase.invoke()
                                                    return@collectLatest
                                                }
                                            }
                                        }
                                    }

                                    is Result.Failure -> {
                                        _codeState.value =
                                            CodeState.Error("Failed to register user")
                                        return@collectLatest
                                    }
                                }
                            }
                        }

                        is Result.Failure -> {
                            _codeState.value = CodeState.Error("Invalid verification code")
                            return@collectLatest
                        }
                    }
                }
            } catch (e: Exception) {
                deleteUserUseCase.invoke()
                _codeState.value = CodeState.Error(e.message ?: "An error occurred")
            }
        }
    }

}