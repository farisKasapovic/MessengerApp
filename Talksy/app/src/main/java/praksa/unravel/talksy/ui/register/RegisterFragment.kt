package praksa.unravel.talksy.ui.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import praksa.unravel.talksy.R
import praksa.unravel.talksy.databinding.FragmentRegisterBinding
import praksa.unravel.talksy.utils.ToastUtils
import java.util.regex.Pattern

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var binding: FragmentRegisterBinding
    private val RC_SIGN_IN = 9001 // Request code for Google Sign-In

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerBtn1.setOnClickListener {
            val username = binding.registerET1.text.toString()
            val email = binding.registerET2.text.toString()
            val phone = binding.registerET3.text.toString()
            val password = binding.registerET4.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && phone.isNotEmpty() && username.isNotEmpty()) {
                if (isValidEmail(email)) {
                    viewModel.startRegistration(
                        email,
                        password,
                        username,
                        "+38761898989",
                        requireActivity()
                    )
                } else {
                    ToastUtils.showCustomToast(requireContext(), "Email is not valid")
                }
            } else {
                // Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_LONG).show()
                ToastUtils.showCustomToast(requireContext(), "All fields are required!")
            }
        }

        binding.registerTV4.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.registerBtn2.setOnClickListener {
            val callbackManager = CallbackManager.Factory.create()
            val loginButton = LoginButton(requireContext())
            loginButton.setPermissions("email", "public_profile")

            loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    val accessToken = loginResult.accessToken
                    viewModel.loginWithFacebook(accessToken)
                }

                override fun onCancel() {
                    ToastUtils.showCustomToast(requireContext(), "Facebook login canceled")
                }

                override fun onError(error: FacebookException) {
                    ToastUtils.showCustomToast(
                        requireContext(),
                        "Facebook login failed: ${error.message}"
                    )
                }
            })

            loginButton.performClick()
        }

        binding.registerBtn3.setOnClickListener {
            val googleSignInClient = GoogleSignIn.getClient(
                requireActivity(),
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            )

            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        observeViewModel()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    viewModel.loginWithGoogle(account)
                }
            } catch (e: ApiException) {
                ToastUtils.showCustomToast(requireContext(), "Google sign-in failed: ${e.message}")
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$")
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    /* private fun observeViewModel() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                ToastUtils.showCustomToast(requireContext(), errorMessage)
            }
        }
    }
    ....
    */
    private fun observeViewModel() {
        // Collect error message
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.collect { errorMessage ->
                    errorMessage?.let {
                        ToastUtils.showCustomToast(requireContext(), it)
                    }
                }
            }
        }

        // Collect registration state
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registrationState.collect { verificationId ->
                    verificationId?.let {
                        val bundle = Bundle().apply {
                            putString("verificationId", it)
                            putString("phone", binding.registerET3.text.toString())
                            putString("username", binding.registerET1.text.toString())
                            putString("email", binding.registerET2.text.toString())
                            putString("password", binding.registerET4.text.toString())
                        }
                        findNavController().navigate(
                            R.id.action_registerFragment_to_codeFragment,
                            bundle
                        )
                    }
                }
            }
        }

        // Collect login success
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginSuccess.collect { success ->
                    if (success) {
                        findNavController().navigate(R.id.action_registerFragment_to_logout)
                    }
                }
            }
        }
    }
}
