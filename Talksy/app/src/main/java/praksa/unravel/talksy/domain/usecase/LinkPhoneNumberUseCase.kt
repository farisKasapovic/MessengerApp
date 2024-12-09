package praksa.unravel.talksy.domain.usecase

import com.google.firebase.auth.PhoneAuthCredential
import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.common.result.Result
import javax.inject.Inject

class LinkPhoneNumberUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend fun invoke(credential: Result<PhoneAuthCredential>): Result<Boolean> {
//        return repository.linkPhoneNumber(credential)
        return when (credential) {
            is Result.success -> repository.linkPhoneNumber(credential.data)
            is Result.failure -> Result.failure(Throwable(" error u usecase"))
        }
    }
}