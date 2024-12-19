package praksa.unravel.talksy.domain.usecase

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.common.result.Result
import javax.inject.Inject

class UploadProfilePictureUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(uri: Uri): Flow<Result<String>> {
        return repository.uploadProfilePicture(uri)
    }
}
