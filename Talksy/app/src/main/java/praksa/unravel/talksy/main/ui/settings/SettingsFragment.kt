package praksa.unravel.talksy.main.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import praksa.unravel.talksy.MainActivity
import praksa.unravel.talksy.R
import praksa.unravel.talksy.databinding.SettingsFragmentBinding
import praksa.unravel.talksy.model.User
import praksa.unravel.talksy.common.result.Result

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var binding: SettingsFragmentBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()

        binding.LogOutTV.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }




        viewModel.fetchCurrentUser()
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SettingsState.Loading -> {}
                is SettingsState.UserDetails -> {
                    bindUserDetails(state.user)
                    state.user.id?.let { viewModel.fetchProfilePicture(it) } // Fetch profile picture
                }
                is SettingsState.ProfilePictureUrl -> bindProfilePicture(state.url)
                is SettingsState.UpdateSuccess -> showToast(state.message)
                is SettingsState.Error -> showError(state.errorMessage)
                is SettingsState.ProfilePictureUrlError -> bindProfilePicture("")
            }
        }
    }

    private fun bindUserDetails(user: User) {
        binding.firstNameTV.text = user.firstName
        binding.lastNameTV.text = user.lastName
        binding.phoneNumberTV.text = user.phone
        binding.usernameTV.text = user.username
    }

    private fun bindProfilePicture(url: String?) {
        if (!url.isNullOrEmpty()) {
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.default_profile_picture)
                .into(binding.profileImageIV)
        } else {
            binding.profileImageIV.setImageResource(R.drawable.default_profile_picture)
        }
    }




    private fun showError(message: String) {
        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
