package praksa.unravel.talksy.ui.signup

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import praksa.unravel.talksy.model.User
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class RegisterViewModel : ViewModel() {

     val firebaseAuth = FirebaseAuth.getInstance()

    private val _registrationSuccess = MutableLiveData<Boolean>()
    val registrationSuccess: LiveData<Boolean> get() = _registrationSuccess

    private val _verificationCodeSent = MutableLiveData<String?>()
    val verificationCodeSent: LiveData<String?> get() = _verificationCodeSent

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private var activityReference: WeakReference<Activity>? = null
    private val firestore = FirebaseFirestore.getInstance()

     fun setActivity(activity: Activity) {
         activityReference = WeakReference(activity)
     }



    private fun sendVerificationCode(phoneNumber: String) {
        val activity = activityReference?.get()
        if (activity == null) {
            _errorMessage.value = "Activity is null"
            return
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity) // WeakReference omogućava sigurno korišćenje
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    linkPhoneNumber(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _errorMessage.value = e.message
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    _verificationCodeSent.value = verificationId
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun linkPhoneNumber(credential: PhoneAuthCredential) {
        firebaseAuth.currentUser?.linkWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _registrationSuccess.value = true
                } else {
                    _errorMessage.value = task.exception?.message
                }
            }
    }



    fun registerUser(email: String, password: String, username: String, phone: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid ?: return@addOnCompleteListener
                    val user = User(
                        id = userId,
                        username = username,
                        email = email,
                        phone = phone,
                        profilePictureUrl = "", // Default empty
                    )
                    saveUserToFirestore(user)
                } else {
                    _errorMessage.value = task.exception?.message
                }
            }
    }

    private fun saveUserToFirestore(user: User) {
        firestore.collection("Users").document(user.id)
            .set(user)
            .addOnSuccessListener {
                _registrationSuccess.value = true
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.message
            }
    }
}
