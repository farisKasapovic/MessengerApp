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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import praksa.unravel.talksy.R
import praksa.unravel.talksy.databinding.FragmentLoginBinding
import praksa.unravel.talksy.ui.signin.LoginViewModel

@Suppress("DEPRECATION")
class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: FragmentLoginBinding
    private lateinit var callbackManager: CallbackManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Initialize Facebook CallbackManager
        callbackManager = CallbackManager.Factory.create()

        // Email/Password login
        binding.loginBtn1.setOnClickListener {
            val email = binding.loginET1.text.toString()
            val password = binding.loginET2.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.loginUser(email, password)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Email and Password cannot be empty",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Facebook login
        binding.loginBtn2.setOnClickListener {
            viewModel.loginWithFacebook(this, callbackManager)
        }
        //Google login
        binding.loginBtn3.setOnClickListener {
            //To be implemented....
        }

        // Navigate to RegisterFragment
        binding.loginTV6.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        observeViewModel()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun observeViewModel() {
        viewModel.loginSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_loginFragment_to_logout)
            }
            Toast.makeText(requireContext(), "Login problem", Toast.LENGTH_LONG).show()
            Log.d(TAG, "GRESKA LOGGIN NIJE SUCCESSFULL")
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(
                    requireContext(),
                    "Authentication failed: $errorMessage",
                    Toast.LENGTH_SHORT
                ).show()
                Log.w("LoginFragment", "signInWithEmail:failure: $errorMessage")
            }
        }
    }
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = viewModel.firebaseAuth.currentUser
        if (currentUser != null) {
            Toast.makeText(requireContext(),"User is signed in",Toast.LENGTH_LONG).show()
        }else {
            Toast.makeText(requireContext(),"User is not signed in",Toast.LENGTH_LONG).show()
        }
    }
}
