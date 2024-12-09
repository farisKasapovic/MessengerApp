package praksa.unravel.talksy.ui.auth

import CodeState
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import praksa.unravel.talksy.domain.usecase.AddUserToDatabaseUseCase
import praksa.unravel.talksy.domain.usecase.LinkPhoneNumberUseCase
import praksa.unravel.talksy.domain.usecase.RegisterUserInAuthUseCase
import praksa.unravel.talksy.domain.usecase.VerifyPhoneNumberWithCodeUseCase
import praksa.unravel.talksy.domain.usecases.DeleteUserFromAuthUseCase
import praksa.unravel.talksy.model.User
import praksa.unravel.talksy.common.exception.Result
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
            Log.d("CodeViewModel","Uslo je u if uslove $verificationId i $code ")
            //_errorMessage.value = "Invalid verification code."
            _codeState.value = CodeState.Error("Invalid verification code")
            return
        }

        viewModelScope.launch {
            try {
                _codeState.value = CodeState.Loading
                Log.d("CodeViewModel","Uslo je u CodeViewModel $verificationId  i $code")
                val credential = verifyPhoneNumberWithCodeUseCase(verificationId, code)

                Log.d("CodeViewModel","vrijednost credentiala $credential")
                val userId = registerUserInAuthUseCase.invoke(email, password)
                Log.d("CodeViewModel", "vrijednost userId DA LI JE SPASEN $userId")
                // OVDJE JE PROBLEM GORE
                val phoneLinked = linkPhoneNumberUseCase.invoke(credential)
                Log.d("CodeViewModel","linkanje telefona $phoneLinked")
//                if (!phoneLinked) {
////                    _errorMessage.value = "Phone verification failed. Please try again."
//                    _codeState.value = CodeState.Error("Phone verification failed. Please try again")
//                    //dodati ovdje delete user
//                    return@launch
//                }
                when(phoneLinked){
                    is Result.success -> {
                        val user = User(
                            email = email,
                            username = username,
                            phone = phoneNumber,
                            profilePicture = ""
                        )
                        addUserToDatabaseUseCase.invoke(user)
                    }
                    is Result.failure -> {
                        _codeState.value = CodeState.Error("Phone verification failed. Please try again")
                        return@launch
                    }
                }



//                val user = User(
//                    email = email,
//                    username = username,
//                    phone = phoneNumber,
//                    profilePicture = ""
//                )
//                addUserToDatabaseUseCase.invoke(user)

//                _registrationComplete.value = true
                _codeState.value = CodeState.Success("Phone number verified")
            } catch (e: Exception) {
                //OVDJE DODAJ DA OBRISES
                deleteUserUseCase.invoke()
//                _errorMessage.value = e.message
                _codeState.value = CodeState.Error(e.message?: "An error occurred")
            }
        }
    }

}
