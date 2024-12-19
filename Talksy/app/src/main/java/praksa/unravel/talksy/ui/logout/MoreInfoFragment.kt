package praksa.unravel.talksy.ui.logout

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import praksa.unravel.talksy.R
import praksa.unravel.talksy.databinding.FragmentMoreInfoBinding
import praksa.unravel.talksy.model.User
import praksa.unravel.talksy.ui.logout.MoreInfoState
import praksa.unravel.talksy.ui.moreinfo.MoreInfoViewModel
import praksa.unravel.talksy.utils.ToastUtils

@Suppress("DEPRECATION")
@AndroidEntryPoint
class MoreInfoFragment : Fragment() {

    private val viewModel: MoreInfoViewModel by viewModels()
    private var _binding: FragmentMoreInfoBinding? = null
    private val binding: FragmentMoreInfoBinding get() = _binding!!

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_more_info, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()

        binding.profileImageIV.setOnClickListener {
            pickImageFromGallery()
        }

        binding.buttonDone.setOnClickListener {
            val firstName = binding.firstNameET.text.toString()
            val lastName = binding.lastNameET.text.toString()
            val bio = binding.BioET.text.toString()
            viewModel.updateUserInfo(firstName, lastName, bio)
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                viewModel.uploadProfilePicture(uri)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.userState.collectLatest {
                when (it) {
                    is MoreInfoState.Loading -> Log.d("MoreInfoFragment", "Loading...")
                    is MoreInfoState.Success -> populateUserData(it.user) //Dodati navigator da predje na fragment u MoreInfoFragmentu
                    is MoreInfoState.Error -> ToastUtils.showCustomToast(
                        requireContext(),
                        it.message
                    )
                }
            }
        }

        lifecycleScope.launch {
            viewModel.profilePictureUrl.collectLatest { url ->
                if (url != null) {
                    Glide.with(this@MoreInfoFragment)
                        .load(url)
                        .placeholder(R.drawable.dots)
                        .into(binding.profileImageIV)
                }
            }
        }
    }

    private fun populateUserData(user: User) {
        binding.firstNameET.setText(user.firstName)
        binding.lastNameET.setText(user.lastName)
        binding.BioET.setText(user.bio)
        binding.phoneNumberTV.text = user.phone
    }
}