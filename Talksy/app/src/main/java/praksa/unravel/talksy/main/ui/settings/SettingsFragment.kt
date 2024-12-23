package praksa.unravel.talksy.main.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import praksa.unravel.talksy.R
import praksa.unravel.talksy.main.ui.contacts.UserStatusViewModel

@AndroidEntryPoint
class SettingsFragment:Fragment() {
    private val activityStatusViewModel: UserStatusViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_fragment,container,false)
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