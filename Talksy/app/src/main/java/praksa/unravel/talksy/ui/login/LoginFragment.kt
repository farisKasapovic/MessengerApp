package praksa.unravel.talksy.ui.login

import android.content.ContentValues.TAG
import android.content.Intent
import android.nfc.Tag
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import praksa.unravel.talksy.R
import praksa.unravel.talksy.databinding.FragmentLoginBinding
import praksa.unravel.talksy.ui.register.RegisterViewModel
import praksa.unravel.talksy.ui.signin.LoginViewModel
import praksa.unravel.talksy.utils.ToastUtils

@Suppress("DEPRECATION")
@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding
    private lateinit var callbackManager: CallbackManager
    private val RC_SIGN_IN = 9001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Initialize Facebook CallbackManager
        callbackManager = CallbackManager.Factory.create()

        // Email/Password login
        binding.loginBtn1.setOnClickListener {
            val email = binding.loginET1.text.toString()
            //val password = binding.loginET2.text.toString()
            val password = binding.loginET21.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.loginUser(email, password)
            } else {
                ToastUtils.showCustomToast(requireContext(), "Email and password cannot be empty")
            }
        }

        // Facebook login
        binding.loginBtn2.setOnClickListener {
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
        //Google login
        binding.loginBtn3.setOnClickListener {
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

        // Navigate to RegisterFragment
        binding.loginTV6.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.forgotPassword.setOnClickListener {
            viewModel.resetPassword(binding.loginET1.text.toString())
        }

        binding.loginTV1.setOnClickListener{
            findNavController().navigate(R.id.action_loginFragment_to_logout)
        }



        observeViewModel()
    }
    //+phone number check

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

    private fun observeViewModel() {


        lifecycleScope.launchWhenStarted {
            viewModel.loginState.collectLatest { state ->
                when (state) {

                    is LoginState.Success -> {
                        ToastUtils.showCustomToast(requireContext(), state.message)
                        findNavController().navigate(R.id.action_loginFragment_to_logout)
                    }

                    is LoginState.Error -> {
                        ToastUtils.showCustomToast(requireContext(), state.errorMessage)
                    }

                    is LoginState.ResetSuccess -> {
                        ToastUtils.showCustomToast(requireContext(), state.resetMessage)
                    }

                    is LoginState.ResetProblem -> {
                        ToastUtils.showCustomToast(requireContext(), state.resetProblem)
                    }

                    else -> Unit

                }
            }
        }

    }


}

