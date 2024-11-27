package praksa.unravel.talksy.ui.logout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import praksa.unravel.talksy.R

class LogoutFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_logout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        val logoutButton: Button = view.findViewById(R.id.buttonLogout)

        logoutButton.setOnClickListener {
            logoutUser()
        }
    }
    /*  public override fun onStart() {
          super.onStart()
          // Check if user is signed in (non-null) and update UI accordingly.
          val currentUser = firebaseAuth.currentUser
          if (currentUser != null) {
              //...
          }
      }*/

    private fun logoutUser() {
        firebaseAuth.signOut()

        // Navigate back to the login screen
        findNavController().navigate(R.id.action_logout_to_loginFragment)
    }
}

