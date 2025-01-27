package praksa.unravel.talksy.data.repositories

import android.net.Uri
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
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import praksa.unravel.talksy.common.asFlow
import praksa.unravel.talksy.common.mapSuccess
import praksa.unravel.talksy.common.result.Result
import praksa.unravel.talksy.model.User
import java.util.concurrent.TimeUnit


class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore
) {


    fun checkEmailExists(email: String): Flow<Result<Boolean>> =
        db.collection("Users")
            .whereEqualTo("email", email)
            .get()
            .asFlow()
            .mapSuccess { data ->
                !data.isEmpty
            }

    fun checkUsernameExists(username: String): Flow<Result<Boolean>> =
        db.collection("Users")
            .whereEqualTo("username", username)
            .get()
            .asFlow()
            .mapSuccess { !it.isEmpty }


    fun checkPhoneNumberExists(phone: String): Flow<Result<Boolean>> =
        db.collection("Users")
            .whereEqualTo("phone", phone)
            .get()
            .asFlow()
            .mapSuccess { !it.isEmpty }

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


    fun verifyPhoneNumberWithCode(
        verificationId: String,
        code: String
    ): Flow<Result<PhoneAuthCredential>> = flow {
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            emit(Result.Success(credential))
        } catch (e: Exception) {
            emit(Result.Failure(e))
        }
    }


    fun registerUserInAuth(email: String, password: String): Flow<Result<String>> {
        return firebaseAuth.createUserWithEmailAndPassword(email, password)
            .asFlow()
            .mapSuccess { authResult ->
                authResult.user?.uid ?: ""
            }
    }


    fun deleteUser(): Flow<Result<Unit>> {
        return firebaseAuth.currentUser?.delete()
            ?.asFlow()
            ?.mapSuccess { Unit }
            ?: flowOf(Result.Failure(Exception("No user is logged in")))
    }


fun addUserToDatabase(user: User): Flow<Result<Unit>> {
    val userId = firebaseAuth.currentUser?.uid ?: return flowOf(Result.Failure(Exception("User not logged in")))

    return db.collection("Users")
        .document(userId)
        .set(user)
        .asFlow()
        .mapSuccess { Unit }
}



    fun linkPhoneNumber(credential: PhoneAuthCredential): Flow<Result<Boolean>> {
        return firebaseAuth.currentUser?.linkWithCredential(credential)
            ?.asFlow()
            ?.mapSuccess { true }
            ?: flowOf(Result.Failure(Exception("No user is logged in")))
    }


    fun linkEmailAndPassword(email: String, password: String): Flow<Result<Boolean>> {
        val credential = EmailAuthProvider.getCredential(email, password)
        return firebaseAuth.currentUser?.linkWithCredential(credential)
            ?.asFlow()
            ?.mapSuccess { true }
            ?: flowOf(Result.Failure(Exception("No user is logged in")))
    }


    fun loginWithFacebook(token: AccessToken): Flow<Result<Boolean>> {
        val credential = FacebookAuthProvider.getCredential(token.token)
        return firebaseAuth.signInWithCredential(credential)
            .asFlow() // Pretvara Task<AuthResult> u Flow<Result<AuthResult>>
            .flatMapConcat { result ->
                when (result) {
                    is Result.Success -> {
                        val user = result.data.user ?: return@flatMapConcat flowOf(
                            Result.Failure(
                                Exception("Facebook login failed: No user returned")
                            )
                        )
                        val userData = User(
                            email = user.email ?: "No email",
                            username = user.displayName ?: "No name",
                            phone = "",
                            profilePicture = user.photoUrl?.toString() ?: "No picture",
                            id = user.uid
                        )
                        addUserToDatabase(userData)
                            .mapSuccess { true }
                    }

                    is Result.Failure -> flowOf(Result.Failure(result.error))
                }
            }
            .catch { e -> emit(Result.Failure(e)) }
    }


    fun loginWithGoogle(account: GoogleSignInAccount): Flow<Result<Boolean>> {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        return firebaseAuth.signInWithCredential(credential)
            .asFlow()
            .flatMapConcat { result ->
                when (result) {
                    is Result.Success -> {
                        val user = result.data.user ?: return@flatMapConcat flowOf(
                            Result.Failure(
                                Exception("Google login failed: No user returned")
                            )
                        )
                        val userData = User(
                            email = user.email ?: "No email",
                            username = user.displayName ?: "No name",
                            phone = "",
                            profilePicture = user.photoUrl?.toString() ?: "No picture",
                            id = user.uid
                        )
                        addUserToDatabase(userData)
                            .mapSuccess { true }
                    }

                    is Result.Failure -> flowOf(Result.Failure(result.error))
                }
            }
            .catch { e -> emit(Result.Failure(e)) }
    }


    fun loginUserWithEmail(email: String, password: String): Flow<Result<Boolean>> {
        firebaseAuth.currentUser?.uid?.let { Log.d("AuthRepo", "vrijednost je $it ") }
        return firebaseAuth.signInWithEmailAndPassword(email, password)
            .asFlow()
            .mapSuccess { true }
    }


    fun sendPasswordResetEmail(email: String): Flow<Result<Unit>> {
        return firebaseAuth.sendPasswordResetEmail(email)
            .asFlow()
            .mapSuccess { Unit }
    }

    // Novo
    fun getUserData(): Flow<Result<User>> =
        db.collection("Users").document(firebaseAuth.currentUser?.uid ?: "")
            .get()
            .asFlow()
            .mapSuccess { snapshot -> snapshot.toObject(User::class.java) ?: User() }

    fun updateUserData(user: User, firstName: String, lastName: String, bio: String): Flow<Result<Unit>> {
        val userId = user.id.ifEmpty { firebaseAuth.currentUser?.uid ?: throw IllegalStateException("User not logged in") }

        return db.collection("Users").document(userId)
            .update("firstName",firstName,"lastName",lastName,"bio",bio)
            .asFlow()
            .mapSuccess { Unit }
    }

    fun uploadProfilePicture(uri: Uri): Flow<Result<String>> = flow {
        try {
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_pictures/${firebaseAuth.currentUser?.uid ?: "unknown_user"}")
            storageRef.putFile(uri).await()

            val downloadUrl = storageRef.downloadUrl.await().toString()

            emit(Result.Success(downloadUrl))
        } catch (e: Exception) {
            emit(Result.Failure(e))
        }
    }









}
