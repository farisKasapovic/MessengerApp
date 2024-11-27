package praksa.unravel.talksy.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import praksa.unravel.talksy.R
import praksa.unravel.talksy.databinding.FragmentRegisterBinding
import praksa.unravel.talksy.ui.signup.RegisterViewModel


class RegisterFragment : Fragment() {

    private lateinit var viewModel: RegisterViewModel
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setActivity(requireActivity())

        binding.registerBtn1.setOnClickListener {
            val username = binding.registerET1.text.toString()
            val email = binding.registerET2.text.toString()
            val phone = binding.registerET3.text.toString()
            val password = binding.registerET4.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && phone.isNotEmpty() && username.isNotEmpty()) {
                viewModel.registerUser(email, password, username, phone)
            } else {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_LONG).show()
            }
        }
        
        binding.registerTV4.setOnClickListener{
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }


        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.registrationSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                findNavController().navigate(R.id.action_registerFragment_to_codeFragment)
            }
        }

        viewModel.verificationCodeSent.observe(viewLifecycleOwner) { verificationId ->
            if (verificationId != null) {
                val bundle = Bundle().apply {
                    putString("verificationId", verificationId)
                    putString("phone",binding.registerET3.text.toString())
                }
                findNavController().navigate(R.id.action_registerFragment_to_codeFragment, bundle)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

     override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = viewModel.firebaseAuth.currentUser
        if (currentUser != null) {
           Toast.makeText(requireContext(),"User is signed in",Toast.LENGTH_LONG).show()
        }
    }

}
