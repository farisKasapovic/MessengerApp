package praksa.unravel.talksy.ui.signin

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import praksa.unravel.talksy.R

class LoginViewModel : ViewModel() {

     val firebaseAuth = FirebaseAuth.getInstance()

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Login with email and password
    fun loginUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginSuccess.value = true
                } else {
                    _loginSuccess.value = false
                    _errorMessage.value = task.exception?.message
                }
            }
    }

    // Login with Facebook
    fun loginWithFacebook(fragment: Fragment, callbackManager: CallbackManager) {
        val facebookLoginButton = LoginButton(fragment.requireContext())
        facebookLoginButton.setPermissions("email", "public_profile")

        facebookLoginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                val token = loginResult.accessToken
                handleFacebookAccessToken(token)
            }

            override fun onCancel() {
                _errorMessage.value = "Facebook login canceled."
            }

            override fun onError(exception: FacebookException) {
                _errorMessage.value = exception.message
            }
        })

        facebookLoginButton.performClick()
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign-in success: Update the login success LiveData
                    _loginSuccess.value = true

                    // Debug: Log the user info
                    val user = firebaseAuth.currentUser
                    Log.d("LoginViewModel", "Firebase user: ${user?.uid}, Email: ${user?.email}")

                } else {
                    // Sign-in failure: Update the error message LiveData
                    _loginSuccess.value = false
                    _errorMessage.value = task.exception?.message

                    // Debug: Log the error
                    Log.e("LoginViewModel", "Firebase sign-in failed", task.exception)
                }
            }
    }


    private fun loginWithGoogle(){

    }

  /*  val RC_SIGN_IN = 101  // Request code for Google Sign-In

    fun getGoogleSignInIntent(activity: Activity): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        return googleSignInClient.signInIntent
    }*/
/*
    fun handleGoogleSignInResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.result
            if (account != null) {
                firebaseAuthWithGoogle(account)
            }
        } catch (e: Exception) {
            _errorMessage.value = e.message
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginSuccess.value = true
                } else {
                    _errorMessage.value = task.exception?.message
                }
            }
    }*/
//}

}
