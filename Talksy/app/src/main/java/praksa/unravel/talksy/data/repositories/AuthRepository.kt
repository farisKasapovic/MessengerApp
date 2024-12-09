package praksa.unravel.talksy.data.repositories

import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import praksa.unravel.talksy.model.User
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import praksa.unravel.talksy.common.result.Result


class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    suspend fun checkEmailExists(email: String): Result<Boolean> = suspendCoroutine { cont ->
        db.collection("Users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    cont.resume(Result.success(true)) // Email postoji
                } else {
                    cont.resume(Result.failure(Exception("Email not found"))) // Email ne postoji
                }
            }
            .addOnFailureListener { exception ->
                cont.resume(Result.failure(exception))
            }
    }

    suspend fun checkUsernameExists(username: String): Result<Boolean> = suspendCoroutine { cont ->
        db.collection("Users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { snapshot ->
                cont.resume(Result.success(!snapshot.isEmpty))
            }
            .addOnFailureListener { exception ->
                cont.resume(Result.failure(exception))
            }
    }

    suspend fun checkPhoneNumberExists(phone: String): Result<Boolean> = suspendCoroutine { cont ->
        db.collection("Users")
            .whereEqualTo("phone", phone)
            .get()
            .addOnSuccessListener { snapshot ->
                cont.resume(Result.success(!snapshot.isEmpty))
            }
            .addOnFailureListener { exception ->
                cont.resume(Result.failure(exception))
            }
    }

    fun sendVerificationCode(
        phoneNumber: String,
        activity: FragmentActivity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyPhoneNumberWithCode(
        verificationId: String,
        code: String
    ): Result<PhoneAuthCredential> = suspendCoroutine { cont ->
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            cont.resume(Result.success(credential))
        } catch (e: Exception) {
            cont.resume(Result.failure(e))
        }
    }

    suspend fun registerUserInAuth(email: String, password: String): Result<String> =
        suspendCoroutine { cont ->
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val userId = result.user?.uid ?: ""
                    cont.resume(Result.success(userId))
                }
                .addOnFailureListener { exception ->
                    cont.resume(Result.failure(exception))
                }
        }

    suspend fun deleteUser(): Result<Unit> = suspendCoroutine { cont ->
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            currentUser.delete()
                .addOnSuccessListener { cont.resume(Result.success(Unit)) }
                .addOnFailureListener { exception -> cont.resume(Result.failure(exception)) }
        } else {
            cont.resume(Result.failure(Exception("No user is logged in")))
        }
    }
 /// dodaoj ovdje nekako id
    suspend fun addUserToDatabase(user: User): Result<Unit> = suspendCoroutine { cont ->
        db.collection("Users")
            .add(user)
            .addOnSuccessListener { cont.resume(Result.success(Unit)) }
            .addOnFailureListener { exception -> cont.resume(Result.failure(exception)) }
    }

    suspend fun linkPhoneNumber(credential: PhoneAuthCredential): Result<Boolean> =
        suspendCoroutine { cont ->
            firebaseAuth.currentUser?.linkWithCredential(credential)
                ?.addOnSuccessListener { cont.resume(Result.success(true)) }
                ?.addOnFailureListener { exception -> cont.resume(Result.failure(exception)) }
                ?: cont.resume(Result.failure(Exception("No user is logged in")))
        }

    suspend fun linkEmailAndPassword(email: String, password: String): Result<Boolean> =
        suspendCoroutine { cont ->
            val credential = EmailAuthProvider.getCredential(email, password)
            firebaseAuth.currentUser?.linkWithCredential(credential)
                ?.addOnSuccessListener { cont.resume(Result.success(true)) }
                ?.addOnFailureListener { exception -> cont.resume(Result.failure(exception)) }
                ?: cont.resume(Result.failure(Exception("No user is logged in")))
        }

    suspend fun loginWithFacebook(token: AccessToken): Result<Boolean> = suspendCoroutine { cont ->
        val credential = FacebookAuthProvider.getCredential(token.token)
        Log.d("AuthRepository","U loginWithFacebook $credential")
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    val userData = User(
                        email = user.email ?: "No email",
                        username = user.displayName ?: "No name",
                        phone = "",
                        profilePicture = user.photoUrl?.toString() ?: "No picture",
                        id = user.uid
                    )
                    GlobalScope.launch {
                        val databaseResult = addUserToDatabase(userData)
                        when(databaseResult){
                            is Result.success -> {
                                cont.resume(Result.success(true))
                            }
                            is Result.failure -> {
                                cont.resume(Result.failure(Throwable("ISPRAVI MEEE E E E ")))
                            }
                        }
                    }
                }else {
                    cont.resume(Result.failure(Exception("Facebook login failed")))
                }
            }
            .addOnFailureListener { exception ->
                cont.resume(Result.failure(exception))
            }
    }


    /*suspend fun loginWithGoogle(account: GoogleSignInAccount): Result<Boolean> = suspendCoroutine { cont ->
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    val userData = User(
                        id = user.uid,
                        username = user.displayName ?: "No name",
                        email = user.email ?: "No email",
                        phone = "",
                        profilePicture = user.photoUrl?.toString() ?: "No picture"
                    )

                    // Launch a coroutine for the suspend function
                    GlobalScope.launch {
                        val databaseResult = addUserToDatabase(userData)
                        when(databaseResult){
                            is Result.success -> {
                                cont.resume(Result.success(true))
                            }
                            is Result.failure -> {
                                cont.resume(Result.failure(Throwable("throw exception")))
                            }
                        }

                    }
                } else {
                    cont.resume(Result.failure(Exception("Google login failed")))
                }
            }
            .addOnFailureListener { exception ->
                cont.resume(Result.failure(exception))
            }
    }*/
    //Verzija2 ?
    suspend fun loginWithGoogle(account: GoogleSignInAccount): Result<Boolean> = suspendCoroutine { cont ->
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    val userData = User(
                        id = user.uid,
                        username = user.displayName ?: "No name",
                        email = user.email ?: "No email",
                        phone = "",
                        profilePicture = user.photoUrl?.toString() ?: "No picture"
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        val databaseResult = addUserToDatabase(userData)

                        withContext(Dispatchers.Main) {
                            when (databaseResult) {
                                is Result.success -> {
                                    cont.resume(Result.success(true))
                                }
                                is Result.failure -> {
                                    cont.resume(Result.failure(Throwable("Database operation failed")))
                                }
                            }
                        }
                    }
                } else {
                    cont.resume(Result.failure(Exception("Google login failed")))
                }
            }
            .addOnFailureListener { exception ->
                cont.resume(Result.failure(exception))
            }
    }



    suspend fun loginUserWithEmail(email: String, password: String): Result<Boolean> =
        suspendCoroutine { cont ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { cont.resume(Result.success(true)) }
                .addOnFailureListener { exception -> cont.resume(Result.failure(exception)) }
        }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = suspendCoroutine { cont ->
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener { cont.resume(Result.success(Unit)) }
            .addOnFailureListener { exception -> cont.resume(Result.failure(exception)) }
    }
}
