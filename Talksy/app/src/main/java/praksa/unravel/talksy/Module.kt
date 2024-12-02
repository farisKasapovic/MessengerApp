package praksa.unravel.talksy

import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.domain.usecase.AddUserToDatabaseUseCase
import praksa.unravel.talksy.domain.usecase.CheckEmailExistsUseCase
import praksa.unravel.talksy.domain.usecase.LinkEmailAndPasswordUseCase
import praksa.unravel.talksy.domain.usecase.LinkPhoneNumberUseCase
import praksa.unravel.talksy.domain.usecase.RegisterUserInAuthUseCase
import praksa.unravel.talksy.domain.usecase.SendVerificationCodeUseCase
import praksa.unravel.talksy.domain.usecase.VerifyPhoneNumberWithCodeUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import praksa.unravel.talksy.domain.usecase.CheckUsernameExistsUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepository(firebaseAuth, firestore)
    }

    @Provides
    fun provideCheckEmailExistsUseCase(repository: AuthRepository): CheckEmailExistsUseCase {
        return CheckEmailExistsUseCase(repository)
    }

    @Provides
    fun provideSendVerificationCodeUseCase(repository: AuthRepository): SendVerificationCodeUseCase {
        return SendVerificationCodeUseCase(repository)
    }

    @Provides
    fun provideLinkPhoneNumberUseCase(repository: AuthRepository): LinkPhoneNumberUseCase {
        return LinkPhoneNumberUseCase(repository)
    }

    @Provides
    fun provideRegisterUserUseCase(repository: AuthRepository): RegisterUserInAuthUseCase {
        return RegisterUserInAuthUseCase(repository)
    }

    @Provides
    fun provideAddUserToDatabaseUseCase(repository: AuthRepository): AddUserToDatabaseUseCase {
        return AddUserToDatabaseUseCase(repository)
    }

    @Provides
    fun provideLinkEmailAndPasswordUseCase(repository: AuthRepository): LinkEmailAndPasswordUseCase {
        return LinkEmailAndPasswordUseCase(repository)
    }

    @Provides
    fun provideVerifyPhoneNumberWithCodeUseCase(repository: AuthRepository): VerifyPhoneNumberWithCodeUseCase {
        return VerifyPhoneNumberWithCodeUseCase(repository)
    }

    @Provides
    fun provideCheckUsernameExistsUseCase(repository: AuthRepository): CheckUsernameExistsUseCase{
        return CheckUsernameExistsUseCase(repository)
    }

}
