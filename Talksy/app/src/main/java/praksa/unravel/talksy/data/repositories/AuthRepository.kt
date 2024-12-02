package praksa.unravel.talksy.data.repositories

import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
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

}
