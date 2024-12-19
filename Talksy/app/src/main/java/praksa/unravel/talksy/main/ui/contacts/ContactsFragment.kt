package praksa.unravel.talksy.main.ui.contacts
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import praksa.unravel.talksy.R
import praksa.unravel.talksy.main.model.Contact
import praksa.unravel.talksy.ui.contacts.ContactsAdapter

@AndroidEntryPoint
class ContactsFragment : Fragment() {

    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactsAdapter
    private val contactsList = mutableListOf<Contact>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.contacts_fragment, container, false)
        val addIcon = view.findViewById<View>(R.id.addContactIV)

        addIcon.setOnClickListener { showPopupWindow(it) }  // ovdje pozovi

        recyclerView = view.findViewById(R.id.contactRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ContactsAdapter(contactsList) { userId, imageView ->
            viewLifecycleOwner.lifecycleScope.launch {
                val profilePictureUrl = viewModel.getProfilePictureUrl(userId)
                Log.d("ContactsFragment","vrijednost profilepictureurlaje $profilePictureUrl")
                Glide.with(requireContext())
                    .load(profilePictureUrl)
                    .placeholder(R.drawable.default_profile_picture)
                    .into(imageView)
            }
        }
//        adapter = ContactsAdapter(contactsList)
        recyclerView.adapter = adapter

        observeViewModel()
        return view
    }



    private fun showPopupWindow(anchorView: View) {
        val popupView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_menu_layout, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true

        popupWindow.showAsDropDown(anchorView, 0, 0)

        val option1 = popupView.findViewById<LinearLayout>(R.id.findPeopleLinear)
        val option2 = popupView.findViewById<LinearLayout>(R.id.invitePeopleLinear)
        val option3 = popupView.findViewById<LinearLayout>(R.id.newContactLinear)


        option1.setOnClickListener {
            Toast.makeText(requireContext(), "Izabrana opcija 1", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }

        option2.setOnClickListener {
            Toast.makeText(requireContext(), "Izabrana opcija 2", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }

        option3.setOnClickListener {
            Toast.makeText(requireContext(), "Izabrana opcija 3", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_contactsFragment_to_newContactFragment)
            popupWindow.dismiss()
        }
    }


    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    is ContactsState.Loading -> {
                        // Prikaz loading indikatora
                    }
                    is ContactsState.Success -> {
                        contactsList.clear()
                        contactsList.addAll(state.contacts)
                        adapter.notifyDataSetChanged()
                    }
                    is ContactsState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    is ContactsState.Empty -> {
                        Toast.makeText(requireContext(), "Nema kontakata", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
