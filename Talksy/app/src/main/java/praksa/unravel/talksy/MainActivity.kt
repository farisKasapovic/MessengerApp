package praksa.unravel.talksy

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.facebook.FacebookSdk
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import praksa.unravel.talksy.main.data.services.NotificationManagerService

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)
        //Initialize fb SDK
        FacebookSdk.setClientToken("4eb1db06218a38f0eb2a6509a6a55846")
        FacebookSdk.sdkInitialize(applicationContext)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController

        val notificationService = NotificationManagerService(this)
        notificationService.registerFCMToken()


    }
}


/*Pon + Uto */
// Code Refactoring (.asFlow())
// Loading statusi
// Notifikacije pogledati jesu li dobre
// Izbrisi sve kontakte
// Izlistaj sve slike
// Mikrofon ikonica, send...
// Grupni Chat !!!!
// Pozivi i VideoChat
// Dark theme


// UI FIXAJ


//Solved
//// U Chat fragmentu treba izbacivati po timestampu...
//// Kada se posalje slika ili voiceMessage da u lastMessage pise slika i to, a ne nista



