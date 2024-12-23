package praksa.unravel.talksy.main.ui.newcontact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import praksa.unravel.talksy.R
import praksa.unravel.talksy.databinding.FragmentRegisterBinding
import praksa.unravel.talksy.databinding.NewContactFragmentBinding
import praksa.unravel.talksy.main.model.Contact

@AndroidEntryPoint
class NewContactFragment : Fragment() {

    private val viewModel: NewContactViewModel by viewModels()

    private var _binding: NewContactFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewContactFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.CreateTV.setOnClickListener {
            val firstName = binding.firstNameET.text.toString()
            val lastName = binding.lastNameET.text.toString()
            val phoneNumber = binding.phoneNumberET.text.toString()
            val username = binding.usernameET.text.toString()

            if (firstName.isBlank() || lastName.isBlank() || phoneNumber.isBlank()) {
                Toast.makeText(requireContext(), "Sva polja su obavezna!", Toast.LENGTH_SHORT).show()
            } else {
                val contact = Contact(
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber
                )
                viewModel.addContact(contact,phoneNumber,username)
            }
        }
        observeViewModel()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is NewContactState.Success -> {
                    Toast.makeText(requireContext(), "Kontakt uspješno dodat!", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
                is NewContactState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is NewContactState.Loading -> {
                    Toast.makeText(requireContext(), "Čuvanje kontakta...", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
