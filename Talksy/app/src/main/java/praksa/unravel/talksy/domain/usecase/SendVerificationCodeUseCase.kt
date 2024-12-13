package praksa.unravel.talksy.domain.usecase

import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.PhoneAuthProvider
import praksa.unravel.talksy.data.repositories.AuthRepository
import javax.inject.Inject

class SendVerificationCodeUseCase @Inject constructor(private val repository: AuthRepository) {

    operator fun invoke(
        phoneNumber: String,
        activity: FragmentActivity,
        callbacks : PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ){
        repository.sendVerificationCode(phoneNumber,activity,callbacks)
    }

}
