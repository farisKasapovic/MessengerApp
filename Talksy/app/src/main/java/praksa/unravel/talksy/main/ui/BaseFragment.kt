package praksa.unravel.talksy.main.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import praksa.unravel.talksy.R
import praksa.unravel.talksy.main.ui.contacts.UserStatusViewModel

@AndroidEntryPoint
class BaseFragment : Fragment() {

    private val activityStatusViewModel: UserStatusViewModel by viewModels()
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.base_fragment, container, false)

        bottomNavigationView = view.findViewById(R.id.bottomNav)

        val navHostFragment = childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.newContactFragment, R.id.directMessageFragment, R.id.groupChatFragment,R.id.profileFragment -> {
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        activityStatusViewModel.setUserOnline()
        Log.d("ContactsFragment","vrijednost: uslo je u onStart()")
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch {
            activityStatusViewModel.setUserOffline()
        }
    }


}
