package praksa.unravel.talksy

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import org.checkerframework.common.subtyping.qual.Bottom
import praksa.unravel.talksy.main.ui.chats.ChatsFragment
import praksa.unravel.talksy.main.ui.premium.PremiumFragment
import praksa.unravel.talksy.main.ui.settings.SettingsFragment
import praksa.unravel.talksy.ui.start.StartFragment

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)
        //Initialize fb SDK
        FacebookSdk.setClientToken("4eb1db06218a38f0eb2a6509a6a55846")
        FacebookSdk.sdkInitialize(applicationContext)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavigationView = findViewById(R.id.bottomNav)
        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment,R.id.startFragment,R.id.codeFragment,R.id.more_info -> {
                    bottomNavigationView.visibility = android.view.View.GONE
                }
                else -> {
                    bottomNavigationView.visibility = android.view.View.VISIBLE
                }
            }
        }

    }



    }

//overridea gmail i to ...

/////Problem sa update firestorea   -->  rijeseno (problem bio sa razlicitim user idovima  uid)
//// Dodao contacts popupWindow
////Dodati navigator da predje na fragment u MoreInfoFragmentu
//Loading indikator.........
////Provjeriti koju funkciju addContact pozivam


// poslije registracije prve u moreInfo, mora kupiti informacije o korisniku a ne ovo mojtabatn
// takodjer klikom na done idem na chats fragment...
//// kada dodam sliku ona treba biti okrugkla e ne kvadrat


//ako je korisnik logovan automatski ga prebaciti na chats fragment sa startfragmenta
//

// NewContact -> ako mi postavimo sliku u svuda se pojavljuje ta slika, ako ne postavimo sliku pojavljuje se ona koju je taj korisnik stavio/defaultna

// last seen recently i to ....  SVAKI PUT KADA SE APP OTVORI

