package praksa.unravel.talksy
/*
// Sta sve treba popraviti

//ViewState, Throwable

// To do:
// 1. Dodati State - razvrstati po klasama
// 2. Dodati exception
// 4. Popraviti layoute finalno
// 5. Dodati Result genericku
// 6. Zamijeniti .await()
//8. return launch

//Finished:
// 3. Dodati Toast ljepsi
// 7. Zamijeniti LiveData u StateFlow  - fix myb ?
* */
class NewCode {
}
/*

package com.example.hook.presentation.authentication.register
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hook.domain.model.User
import com.example.hook.domain.repository.UserRepository
import com.example.hook.domain.usecase.SaveUserToFirebaseUseCase
import com.example.hook.domain.usecase.SignInWIthCredentialUseCase
import com.example.hook.domain.usecase.InputFieldValidator
import com.facebook.AccessToken
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val inputFieldValidator: InputFieldValidator,
    private val signInWithCredentialUseCase: SignInWIthCredentialUseCase,
    private val userRepository: UserRepository,
    private val saveUserToFirestore : SaveUserToFirebaseUseCase
) : ViewModel() {

    private val _registerState: MutableStateFlow<RegisterState> = MutableStateFlow(RegisterState.Initial)
    val registerState = _registerState.asStateFlow()

    fun validateUserInput(username: String, email: String, phone: String, password: String) {
        viewModelScope.launch {
            val user = User(username, email, phone, password, null)

            val areFieldsValid = inputFieldValidator(user)
            val state = if (areFieldsValid.isSuccess) {
                RegisterState.ValidInput
            } else {
                RegisterState.Error(areFieldsValid.exceptionOrNull() ?: Throwable("Undefined error"))
            }

            _registerState.value = state
        }
    }

    fun resetValidationState() {
        _registerState.value = RegisterState.Initial
    }

    fun registerWithFacebook(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        authenticateWithCredential(credential)
    }

    fun registerWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        authenticateWithCredential(credential)
    }

    private fun authenticateWithCredential(credential: AuthCredential) = viewModelScope.launch {
        val result = signInWithCredentialUseCase.signInWithCredential(credential)
        if (result.isSuccess) {
            _registerState.value = RegisterState.CredentialSignInSuccess
        } else if (result.isFailure) {
            _registerState.value = RegisterState.Error(result.exceptionOrNull() ?: Throwable("Undefined exception."))
        }
    }

    fun saveUserLocally(user: User) {
        viewModelScope.launch {
            userRepository.saveUserToLocal(user)
        }
    }
}


-----------------------------------------

package com.example.hook.presentation.authentication.register

sealed class RegisterState {
    data object Initial : RegisterState()
    data object Loading : RegisterState()
    data object ValidInput : RegisterState()
    data object CredentialSignInSuccess : RegisterState()
    data class Error(val error: Throwable) : RegisterState()
}

----------------------------------------

package com.example.hook.presentation.authentication.register

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hook.R
import com.example.hook.databinding.CustomToastBinding
import com.example.hook.databinding.FragmentRegisterBinding
import com.example.hook.domain.model.User
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient
    private val GOOGLE_SIGN_IN_REQUEST_CODE = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        setObservers()
        setListeners()
        setupGoogleSignInClient()
        // setupPhoneNumberEditText()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupPhoneNumberEditText() {
        binding.registrationPhoneNumberEditText.apply {
            val phonePrefix = "+3876"
            setText(phonePrefix)
            setSelection(phonePrefix.length)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!s.toString().startsWith(phonePrefix)) {
                        setText(phonePrefix)
                        setSelection(phonePrefix.length)
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun setListeners() {
        binding.signUpButton.setOnClickListener {
            val email = binding.registrationEmailEditText.text.toString().trim()
            val password = binding.registrationPasswordEditText.editText?.text.toString().trim()
            val username = binding.registrationUsernameEditText.text.toString().trim()
            val phone = binding.registrationPhoneNumberEditText.text.toString().trim()
            viewModel.validateUserInput(username, email, phone, password)
        }
        binding.fbButton.setOnClickListener {
            setupFacebookLogin()
        }
        binding.googleButton.setOnClickListener {
            initiateGoogleSignIn()
        }
        binding.goToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun setObservers() {
        lifecycleScope.launch {
            viewModel.registerState.collectLatest { state ->
                when (state) {
                    is RegisterState.CredentialSignInSuccess -> showCredentialSignInSuccess()
                    RegisterState.Initial -> showInitial()
                    RegisterState.Loading -> showLoading()
                    RegisterState.ValidInput -> showValidInput()
                    is RegisterState.Error -> showError(state.error)
                }
            }
        }
        /*viewLifecycleOwner.lifecycleScope.launch {
            viewModel.validationState.collect { result ->
                when (result) {
                    is ValidationResult.Success -> {
                        val email = binding.registrationEmailEditText.text.toString().trim()
                        val password =
                            binding.registrationPasswordEditText.editText?.text.toString().trim()
                        val username = binding.registrationUsernameEditText.text.toString().trim()
                        val phone = binding.registrationPhoneNumberEditText.text.toString().trim()
                        val user = User(username, email, phone, password, null)
                        viewModel.saveUserLocally(user)
                        showSuccessAndNavigate(
                            "Proceeding to phone authentication",
                            R.id.action_registerFragment_to_phoneVerification
                        )
                    }

                    is ValidationResult.Idle -> {
                    }

                    is ValidationResult.BlankFields -> {
                        showError(result.message)
                        viewModel.resetValidationState()
                    }

                    is ValidationResult.PhoneNumberRegistered -> {
                        showError(result.message)
                        viewModel.resetValidationState()
                    }

                    is ValidationResult.InvalidPassword -> {
                        showError(result.message)
                        viewModel.resetValidationState()
                    }

                    is ValidationResult.InvalidEmailFormat -> {
                        showError(result.message)
                        viewModel.resetValidationState()
                    }

                    is ValidationResult.UsernameTaken -> {
                        showError(result.message)
                        viewModel.resetValidationState()
                    }

                    is ValidationResult.InvalidPhoneNumber -> {
                        showError(result.message)
                        viewModel.resetValidationState()
                    }

                    is ValidationResult.WeakPassword -> {
                        showError(result.message)
                        viewModel.resetValidationState()
                    }

                    is ValidationResult.EmailTaken -> {
                        showError(result.message)
                        viewModel.resetValidationState()
                    }

                }

            }
        }*/
    }

    private fun showSuccess(message: String) {
        showToast(message)
    }

    private fun showError(error: Throwable) {
        error.message?.let { errorMsg ->
            showToast(errorMsg)
        }
    }

    private var isToastVisible = false
    private fun showToast(message: String) {
        if (isToastVisible) return
        isToastVisible = true
        val toastBinding = CustomToastBinding.inflate(layoutInflater)
        toastBinding.toastMessage.text = message
        Toast(requireContext()).apply {
            duration = Toast.LENGTH_LONG
            view = toastBinding.root
            setGravity(Gravity.TOP, 0, 100)
            show()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            isToastVisible = false
        }, 3500)
    }

    private lateinit var callbackManager: CallbackManager
    private fun setupFacebookLogin() {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    val token = result.accessToken
                    viewModel.registerWithFacebook(token)
                }

                override fun onCancel() {
                    showError("Facebook login cancelled")
                }

                override fun onError(error: FacebookException) {
                    showError("Facebook login error: ${error.message}")
                }
            })
    }

    private fun showSuccessAndNavigate(message: String, destinationId: Int) {
        showSuccess(message)
        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(destinationId)
        }, 3500)
    }

    // prebaciti tamo gdje je sva logika za google sign in i kontekst ide sa @ApplicationContext da se injecta u klasu
    private fun setupGoogleSignInClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    private fun initiateGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                viewModel.registerWithGoogle(idToken)
            } else {
                showError("Failed to retrieve Google ID token")
            }
        } catch (e: ApiException) {
            showError("Google Sign-In failed: ${e.message}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FacebookSdk.getCallbackRequestCodeOffset()) {
            if (::callbackManager.isInitialized) {
                callbackManager.onActivityResult(requestCode, resultCode, data)
            }
        }
        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
        }
    }
}
--------------------------------------------------------


package com.example.hook.presentation.authentication.register

sealed class AuthResult {
    object Idle : AuthResult()
    object Loading : AuthResult()
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}
-------------------------------------------------------


package com.example.hook.common.exception

class BlankFieldsException: Throwable(message = "Blank fields.")


DIO KODA
lass FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository

) {

    suspend fun registerUser(
        username: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Result<Unit> {
            val currentUser =
                auth.currentUser ?: error(Exception("No authenticated user to link credentials"))
            val emailCredential =
                com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
            currentUser.linkWithCredential(emailCredential).await()
            val userId = currentUser.uid
            val something = currentUser.getIdToken(true)
            val tokenResult = currentUser.getIdToken(true).addOnSuccessListener { token ->
                if (token != null) {
                    userRepository.updateToken(token.token.orEmpty())
                } else {
                    error("Failed to fetch Firebase token")
                }
            }
            saveUserToFirestore(username, email, phoneNumber, firebaseToken, userId)

            /*val userData = hashMapOf(
                "username" to username,
                "phoneNumber" to phoneNumber,
                "email" to email,
                "firebaseToken" to firebaseToken

            )
            try {
                firestore.collection("users")
                    .document(userId)
                    .set(userData)
                    .await()

                userRepository.updateToken(firebaseToken)
                Result.success(Unit)
            } catch (firestoreException: Exception) {
                Result.failure(Exception("Failed to save user data to Firestore: ${firestoreException.message}"))
            }*/
    }
 */



