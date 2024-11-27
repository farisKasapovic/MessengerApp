package praksa.unravel.talksy.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import praksa.unravel.talksy.R
import praksa.unravel.talksy.databinding.FragmentCodeBinding

class CodeFragment : Fragment() {

    private lateinit var viewModel: CodeViewModel
    private lateinit var binding: FragmentCodeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_code, container, false)
        viewModel = ViewModelProvider(this)[CodeViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the verification ID from arguments
        val verificationId = arguments?.getString("verificationId")
        val phoneNumber = arguments?.getString("phone")
        binding.tvDescription.text = "We've sent the code via SMS to $phoneNumber"
        if (verificationId != null) {
            viewModel.setVerificationId(verificationId)
        } else {
            Log.e("CodeFragment", "Verification ID is null")
        }

        setupEditTexts()

        binding.tvResendCode.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Resend functionality not yet implemented.",
                Toast.LENGTH_SHORT
            ).show()
        }

        observeViewModel()
    }

    private fun setupEditTexts() {
        val editTexts = arrayOf(
            binding.etCode1,
            binding.etCode2,
            binding.etCode3,
            binding.etCode4,
            binding.etCode5,
            binding.etCode6
        )

        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        if (i < editTexts.size - 1) {
                            editTexts[i + 1].requestFocus()
                        } else {
                            editTexts[i].clearFocus()
                            val code = editTexts.joinToString("") { it.text.toString() }
                            viewModel.verifyCode(code)
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun observeViewModel() {
        viewModel.verificationSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Phone number verified!", Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigate(R.id.action_codeFragment_to_logout)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                Log.e("CodeFragment", "Verification error: $errorMessage")
            }
        }
    }
}
