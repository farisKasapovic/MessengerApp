package praksa.unravel.talksy.main.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import praksa.unravel.talksy.R

class BaseFragment : Fragment() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_base, container, false)

        // Inicijalizacija BottomNavigationView
        bottomNavigationView = view.findViewById(R.id.bottomNav)

        val navHostFragment = childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Povezivanje BottomNavigationView sa NavController-om
        bottomNavigationView.setupWithNavController(navController)

        return view
    }
}
