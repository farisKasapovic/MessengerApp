package praksa.unravel.talksy.ui.register

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import praksa.unravel.talksy.R
import praksa.unravel.talksy.databinding.FragmentRegisterBinding
import praksa.unravel.talksy.utils.ToastUtils

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private val viewModel: RegisterViewModel by viewModels()
    private var _binding: FragmentRegisterBinding? = null
    private val binding: FragmentRegisterBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerBtn1.setOnClickListener {
            val username = binding.registerET1.text.toString()
            val email = binding.registerET2.text.toString()
            val phone = binding.registerET3.text.toString()
            val password = binding.registerET4.text.toString()
            Log.d("telefon","telefon $phone")
            // +387 62 232323  +387 62 121212   +387 62 343434
            viewModel.startRegistration(
                email,
                password,
                username,
                phone, //phonenum
                requireActivity()
            )
        }

        binding.registerTV4.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.registerBtn2.setOnClickListener {
            facebook()
        }

        binding.registerBtn3.setOnClickListener {
            google()
        }
        binding.registerTV3.setOnClickListener{
            findNavController().navigate(R.id.action_registerFragment_to_codeFragment)
        }

        observeViewModel()
    }

    @Deprecated("Deprecated in Java")
    //Registeronactivitycallback
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


    private fun facebook() {
        val callbackManager = CallbackManager.Factory.create()
        val loginButton = LoginButton(requireContext())
        loginButton.setPermissions("email", "public_profile")



        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                val accessToken = loginResult.accessToken
                viewModel.loginWithFacebook(accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
                ToastUtils.showCustomToast(requireContext(), "Facebook login canceled")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
                Log.d("RegisterFragment", " onErrorFacebookLogin")
                ToastUtils.showCustomToast(
                    requireContext(),
                    "Facebook login failed: ${error.message}"
                )
            }
        })

        loginButton.performClick()
    }

    private fun google() {
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


    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.registerState.collectLatest { state ->
                when (state) {
                    is RegisterState.EmailAlreadyExists -> {
                        ToastUtils.showCustomToast(requireContext(), "Email already exists")
                    }

                    is RegisterState.UsernameAlreadyExists -> {
                        ToastUtils.showCustomToast(requireContext(), "Username already exists.")
                    }

                    is RegisterState.PhoneNumberAlreadyExists -> {
                        ToastUtils.showCustomToast(requireContext(), "Phone number already exists.")
                    }

                    is RegisterState.VerificationIdSuccess -> {
                        navigateToCodeFragment(state.verificationId)
                    }

                    is RegisterState.FacebookSuccess -> {
                        findNavController().navigate(R.id.action_registerFragment_to_more_info)
                    }

                    is RegisterState.GoogleSuccess -> {
                        findNavController().navigate(R.id.action_registerFragment_to_more_info)
                    }

                    is RegisterState.Failed -> {
                        ToastUtils.showCustomToast(requireContext(), "${state.errorMessage}failano ")
                        Log.d("fail","failano")
                    }

                    else -> Unit
                }
            }
        }
    }

//    private fun navigateToCodeFragment(verificationId: String) {
//        val bundle = Bundle().apply {
//            putString("verificationId", verificationId)
//            putString("phone", binding.registerET3.text.toString())
//            putString("username", binding.registerET1.text.toString())
//            putString("email", binding.registerET2.text.toString())
//            putString("password", binding.registerET4.text.toString())
//        }
//        findNavController().navigate(R.id.action_registerFragment_to_codeFragment, bundle)
//    }

    private fun navigateToCodeFragment(verificationId: String) {
        // Instead of bundle we use this metod
        val action = RegisterFragmentDirections.actionRegisterFragmentToCodeFragment(
            verificationId = verificationId,
            phone = binding.registerET3.text.toString(),
            username = binding.registerET1.text.toString(),
            email = binding.registerET2.text.toString(),
            password = binding.registerET4.text.toString()
        )
        findNavController().navigate(action)
    }


    companion object {
        private val RC_SIGN_IN = 9001 // Request code for Google Sign-In
    }

}

