package praksa.unravel.talksy.ui.auth

import CodeState
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LongDef
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import praksa.unravel.talksy.R
import praksa.unravel.talksy.databinding.FragmentCodeBinding
import praksa.unravel.talksy.ui.register.RegisterViewModel
import praksa.unravel.talksy.utils.ToastUtils

@AndroidEntryPoint
class CodeFragment : Fragment() {

    private val viewModel: CodeViewModel by viewModels()
    private lateinit var binding: FragmentCodeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_code, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = CodeFragmentArgs.fromBundle(requireArguments())
        val verificationId = args.verificationId
        val email = args.email
        val password = args.password
        val username = args.username
        val phone = args.phone


        Log.d("CodeFragment", "Your verification id is $verificationId")
        binding.tvDescription.text = "We've sent the code via SMS to $phone"
        setupEditTexts(verificationId, email, password, username, phone)

        binding.tvResendCode.setOnClickListener {
            ToastUtils.showCustomToast(requireContext(), "Resend functionallity not addded yet")
        }

        observeViewModel()
    }

    // added deleting
    @SuppressLint("ClickableViewAccessibility")
    private fun setupEditTexts(
        verificationId: String,
        email: String,
        password: String,
        username: String,
        phone: String
    ) {
        val editTexts = arrayOf(
            binding.etCode1,
            binding.etCode2,
            binding.etCode3,
            binding.etCode4,
            binding.etCode5,
            binding.etCode6
        )

        for (i in editTexts.indices) {
            val editText = editTexts[i]

            // Dodaj TextWatcher za prelazak na sledeće polje
            editText.addTextChangedListener(object : TextWatcher {
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
                            editText.clearFocus()
                            val code = editTexts.joinToString("") { it.text.toString() }
                            viewModel.verifyCodeAndRegister(
                                code,
                                verificationId,
                                email,
                                password,
                                username,
                                phone
                            )
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            // Automatsko brisanje pri kliku
            editText.setOnTouchListener { _, _ ->
                if (editText.hasFocus().not()) {
                    editText.text.clear()
                    editText.requestFocus()
                }
                false
            }


            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (editText.text.isEmpty() && i > 0) {
                        editTexts[i - 1].requestFocus()
                    }
                }
                false
            }
        }
    }


    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.codeState.collect { state ->
                when (state) {
                    is CodeState.Idle -> {
                        //
                    }

                    is CodeState.Loading -> {
                        //
                    }

                    is CodeState.Success -> {
                        ToastUtils.showCustomToast(requireContext(), state.message)
                        findNavController().navigate(R.id.action_codeFragment_to_logout)
                    }

                    is CodeState.Error -> {
                        ToastUtils.showCustomToast(requireContext(), state.message)
                    }

                    else -> Unit
                }

            }

        }


    }
}
