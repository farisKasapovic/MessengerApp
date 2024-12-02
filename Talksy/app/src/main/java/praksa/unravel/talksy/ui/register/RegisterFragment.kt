package praksa.unravel.talksy.ui.register

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import praksa.unravel.talksy.R
import praksa.unravel.talksy.databinding.FragmentRegisterBinding
import java.util.regex.Pattern

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var binding: FragmentRegisterBinding

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
                    viewModel.startRegistration(email, password, username, "+38762333333", requireActivity())
                } else {
                    Toast.makeText(requireContext(), "Email is not valid", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_LONG)
                    .show()
            }
        }

        binding.registerTV4.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        observeViewModel()
    }

    private fun isValidEmail(email: String): Boolean {
        val pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$")
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }


    private fun observeViewModel() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.registrationState.observe(viewLifecycleOwner) { verificationId ->
            Log.d("Register fragment","Your verification id is $verificationId") // Top dobijem ga
            if (verificationId != null) {
                val bundle = Bundle().apply {
                    putString("verificationId", verificationId)
                    putString("phone",binding.registerET3.text.toString())
                    putString("username",binding.registerET1.text.toString())
                    putString("email",binding.registerET2.text.toString())
                    putString("password",binding.registerET4.text.toString())
                }
                findNavController().navigate(R.id.action_registerFragment_to_codeFragment,bundle)
            }

        }


    }


}
