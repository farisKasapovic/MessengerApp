package praksa.unravel.talksy.ui.logout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import praksa.unravel.talksy.R
import praksa.unravel.talksy.utils.ToastUtils

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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Back navigation X
            }
        })
    }

    private fun logoutUser() {
        firebaseAuth.signOut()

        // Navigate back to the login screen
        findNavController().navigate(R.id.action_logout_to_loginFragment)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            ToastUtils.showCustomToast(requireContext(),"User with this email ${currentUser.email} is logged in")
        } else
            ToastUtils.showCustomToast(requireContext(), "User is not logged in")
    }
}

// Screen
// 1. Edit profile -> (ime i prezime, slika,bio)   ->  Poslije Registracije
//                                                 ->  Kasnije kada bude trebalo EditProfile
// 2. MainScreen -> Meni, TabLayout, ViewPager, RecyclerView ....
// 3.
//
//
//


