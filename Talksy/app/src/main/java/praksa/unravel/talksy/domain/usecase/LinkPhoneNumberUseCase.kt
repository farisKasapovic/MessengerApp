package praksa.unravel.talksy.domain.usecase

import com.google.firebase.auth.PhoneAuthCredential
import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.common.result.Result
import javax.inject.Inject

class LinkPhoneNumberUseCase @Inject constructor(private val repository: AuthRepository) {
        operator fun invoke(credential: PhoneAuthCredential): Flow<Result<Boolean>> {
        return repository.linkPhoneNumber(credential)
    }
}