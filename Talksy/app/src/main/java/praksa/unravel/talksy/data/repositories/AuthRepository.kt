package praksa.unravel.talksy.data.repositories

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
import kotlinx.coroutines.tasks.await
import praksa.unravel.talksy.model.User
import java.util.concurrent.TimeUnit

class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    //Provjera da li email postoji u bazi
    suspend fun checkEmailExists(email: String): Boolean {
        val snapshot = db.collection("Users")        // vraca QuerySnapshot objekat, niz dokumenata
            .whereEqualTo(
                "email",
                email
            )                    // ako ne postoji nijedan dokument snapshot ce biti prazan
            .get()
            .await()
        return !snapshot.isEmpty
    }
    //Provjera za username
    suspend fun checkUsernameExists(username: String):Boolean{
        val nameCheck = db.collection("Users")
            .whereEqualTo("username",username)
            .get()
            .await()
        return !nameCheck.isEmpty
    }

    //Provjera da li postoji broj isti u bazi
    suspend fun checkPhoneNumberExists(phone:String):Boolean{
        val snapshot = db.collection("Users")
            .whereEqualTo("phone",phone)
            .get()
            .await()
        return !snapshot.isEmpty
    }



    // Pocetak telefonske autentifikacije
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
        PhoneAuthProvider.verifyPhoneNumber(options)    //Calling firebas verify function
    }

    // Podudaranje koda i verificationId
    suspend fun verifyPhoneNumberWithCode(
        verificationId: String,
        code: String
    ): PhoneAuthCredential {
        return PhoneAuthProvider.getCredential(verificationId, code)
    }

    //Registracija korisnika u FirebaseAuth
    suspend fun registerUserInAuth(email: String, password: String): String {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw Exception("Failed to create user")
    }


    //Delete current user
    suspend fun deleteUser() {
        try {
            // Get the current user
            val currentUser = firebaseAuth.currentUser
            currentUser?.delete()?.await() // Delete the user

            // Optionally, remove the user's record from Firestore
           // currentUser?.uid?.let { uid ->
             //   db.collection("Users").document(uid).delete().await()
           // }

            // Log success
            println("User successfully deleted from FirebaseAuth and Firestore.")
        } catch (e: Exception) {
            throw Exception("Failed to delete user: ${e.message}")
        }
    }


    // Dodavanje korisnika u Firestore
    suspend fun addUserToDatabase(user: User) {
        db.collection("Users")
            //.document(user.id)
           // .set(user)
            .add(user)
            .await()
    }

    // Povezivanje telefonskog broja sa emailom
    suspend fun linkPhoneNumber(credential: PhoneAuthCredential): Boolean {
        return try {
            val result = firebaseAuth.currentUser?.linkWithCredential(credential)?.await()
            result != null
        } catch (e: Exception) {
            throw e
        }
    }

    // Povezivanje emaila sa telefonskim brojem
    suspend fun linkEmailAndPassword(email: String, password: String): Boolean {
        val credential = EmailAuthProvider.getCredential(email, password)
        return try {
            val result = firebaseAuth.currentUser?.linkWithCredential(credential)?.await()
            result != null
        } catch (e: Exception) {
            throw e
        }
    }


    suspend fun loginWithFacebook(token: AccessToken): Boolean {
        val credential = FacebookAuthProvider.getCredential(token.token)
        return try {
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user
            user?.let {
                val userData = User(
                    email = it.email ?: "No email",
                    username = it.displayName ?: "No name",
                    phone = "", // Broj telefona nije dostupan iz Facebook API-ja
                    profilePicture = it.photoUrl?.toString() ?: "No picture",
                    id = it.uid
                )
                addUserToDatabase(userData) // Koristimo vašu funkciju
            }
            true
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun loginWithGoogle(account: GoogleSignInAccount): Boolean {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        return try {
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user
            user?.let {
                val userData = User(
                    id = it.uid,
                    username = it.displayName ?: "No name",
                    email = it.email ?: "No email",
                    phone = "", // Ako imate broj, možete ga dodati
                    profilePicture = it.photoUrl?.toString() ?: "No picture",
                )
                addUserToDatabase(userData) // Koristimo vašu funkciju
            }
            true
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun loginUserWithEmail(email: String, password: String): Boolean {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user != null
        } catch (e: Exception) {
            throw Exception(e.message ?: "Login failed")
        }
    }

suspend fun sendPasswordResetEmail(email:String){
    try{ // MORA PROVJERITI DA LI UOPSTE POSTOJI TAJ MAIL
        firebaseAuth.sendPasswordResetEmail(email).await()
    }catch (e: Exception){
        throw Exception("Failed to send resend email: ${e.message}")
    }
}



// 1. MORA PROVJERITI DA LI UOPSTE POSTOJI TAJ MAIL
    // 2. remember me ...

}
