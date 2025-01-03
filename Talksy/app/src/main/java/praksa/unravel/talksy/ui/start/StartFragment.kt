package praksa.unravel.talksy.ui.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import praksa.unravel.talksy.R
import praksa.unravel.talksy.databinding.FragmentStartBinding


class StartFragment : Fragment() {

    lateinit var binding: FragmentStartBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_start,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.continueBtn.setOnClickListener{
            findNavController().navigate(R.id.action_startFragment_to_loginFragment)
        }
    }

    override fun onStart() {
        super.onStart()
     val auth = FirebaseAuth.getInstance()
        val currentUser=auth.currentUser
        if(currentUser!=null){
            findNavController().navigate(R.id.action_startFragment_to_baseFragment)
        }

    }

}




