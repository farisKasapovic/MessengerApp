package praksa.unravel.talksy

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import praksa.unravel.talksy.main.data.repositories.ContactsRepository
import praksa.unravel.talksy.main.domain.usecase.AddContactUseCase
import praksa.unravel.talksy.main.domain.usecase.CheckUserExistsByPhoneOrUsername
import praksa.unravel.talksy.main.domain.usecase.GetContactsUseCase
import praksa.unravel.talksy.main.domain.usecase.GetProfilePictureUrlUseCase
//import praksa.unravel.talksy.main.domain.usecase.GetUserStatusUseCase
//import praksa.unravel.talksy.main.domain.usecase.UpdateUserStatusUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContactsModule {


    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideContactsRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): ContactsRepository {
        return ContactsRepository(firebaseAuth, firestore,storage)
    }

    @Provides
    fun provideAddContactUseCase(repository: ContactsRepository): AddContactUseCase {
        return AddContactUseCase(repository)
    }

    @Provides
    fun provideGetContactsUseCase(repository: ContactsRepository): GetContactsUseCase {
        return GetContactsUseCase(repository)
    }

    @Provides
    fun provideCheckUserExistsByPhoneOrUsername(repository: ContactsRepository): CheckUserExistsByPhoneOrUsername {
        return CheckUserExistsByPhoneOrUsername(repository)
    }

    @Provides
    fun provideGetProfilePictureUrlUseCase(repository: ContactsRepository):GetProfilePictureUrlUseCase{
        return GetProfilePictureUrlUseCase(repository)
    }

}
