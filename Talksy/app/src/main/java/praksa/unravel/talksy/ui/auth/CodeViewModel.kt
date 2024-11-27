package praksa.unravel.talksy.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class CodeViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _verificationSuccess = MutableLiveData<Boolean>()
    val verificationSuccess: LiveData<Boolean> get() = _verificationSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private var verificationId: String? = null

    fun setVerificationId(id: String) {
        verificationId = id
    }

    fun verifyCode(code: String) {
        if (verificationId != null && code.length == 6) {
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        } else {
            _errorMessage.value = "Invalid verification code"
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.currentUser?.linkWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _verificationSuccess.value = true
                } else {
                    _verificationSuccess.value = false
                    _errorMessage.value = task.exception?.message
                }
            }
    }
}
