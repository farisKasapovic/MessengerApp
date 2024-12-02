package praksa.unravel.talksy.ui.register

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import praksa.unravel.talksy.domain.usecase.*
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val checkEmailExistsUseCase: CheckEmailExistsUseCase,
    private val checkUsernameExistsUseCase: CheckUsernameExistsUseCase,
    private val sendVerificationCodeUseCase: SendVerificationCodeUseCase
) : ViewModel() {

    private val _registrationState = MutableLiveData<String>()
    val registrationState: LiveData<String> get() = _registrationState

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun startRegistration(
        email: String, password: String, username: String, phoneNumber: String, activity: FragmentActivity
    ) {
        //viewModelScope.launch {

            viewModelScope.launch {
            try {
                // Provjeram da li email postoji
                val emailExists = checkEmailExistsUseCase.invoke(email)
                if (emailExists) {
                    _errorMessage.value = "Email already exists."
                    return@launch
                }

                val usernameExists = checkUsernameExistsUseCase.invoke(username)
                if(usernameExists){
                    _errorMessage.value = "Username already exists"
                    return@launch
                }


                // Pokretanje telefonske autentifikacije
                sendVerificationCodeUseCase.invoke(phoneNumber, activity, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        _errorMessage.value = "Phone verification failed: ${e.message}"
                    }

                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        _registrationState.value = verificationId
                    }
                })
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
        //}
    }}

    /*fun verifyPhoneNumberWithCode(verificationId: String, code: String, email: String, password: String, username: String,phoneNumber: String) {
        viewModelScope.launch {
            try {
                val credential = verifyPhoneNumberWithCodeUseCase(verificationId, code)
                linkPhoneNumber(credential, email, password, username, phoneNumber )
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    private fun linkPhoneNumber(credential: PhoneAuthCredential, email: String, password: String, username: String,phoneNumber: String) {
        viewModelScope.launch {
            try {
                val phoneLinked = linkPhoneNumberUseCase.invoke(credential)
                if (!phoneLinked) {
                    _errorMessage.value = "Failed to link phone number."
                    return@launch
                }

                // Registruj email i poveži sa telefonom
                val userId = registerUserInAuthUseCase.invoke(email, password)
                val emailLinked = linkEmailAndPasswordUseCase(email, password)

                if (!emailLinked) {
                    _errorMessage.value = "Failed to link email and password."
                    return@launch
                }

                // Dodaj korisnika u Firestore
                val user = User(id = "", email = email, username = username, phone =phoneNumber )
                addUserToDatabaseUseCase.invoke(user)

                _registrationState.value = "Registration successful."
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }*/





/*
    fun registerUser(email: String, password: String, username: String, phone: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid ?: return@addOnCompleteListener
                    val user = User(
                        id = userId,
                        username = username,
                        email = email,
                        phone = phone
                    )
                    saveUserToFirestore(user)
                } else {
                    _errorMessage.value = task.exception?.message
                }
            }
    }





    private fun sendVerificationCode(phoneNumber: String) {
        val activity = activityReference?.get()
        if (activity == null) {
            _errorMessage.value = "Activity is null"
            return
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity) // WeakReference omogućava sigurno korišćenje
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    linkPhoneNumber(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _errorMessage.value = e.message
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    _verificationCodeSent.value = verificationId
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun linkPhoneNumber(credential: PhoneAuthCredential) {
        firebaseAuth.currentUser?.linkWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _registrationSuccess.value = true
                } else {
                    _errorMessage.value = task.exception?.message
                }
            }
    }





    private fun saveUserToFirestore(user: User) {
        firestore.collection("Users").document(user.id)
            .set(user)
            .addOnSuccessListener {
                _registrationSuccess.value = true
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.message
            }
    }*/
