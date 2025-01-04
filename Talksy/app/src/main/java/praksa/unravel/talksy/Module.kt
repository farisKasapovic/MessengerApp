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
import dagger.hilt.internal.processedrootsentinel.ProcessedRootSentinel
import praksa.unravel.talksy.domain.usecase.CheckPhoneNumberExistsUseCase
import praksa.unravel.talksy.domain.usecase.CheckUsernameExistsUseCase
import praksa.unravel.talksy.domain.usecase.LoginUserUseCase
import praksa.unravel.talksy.domain.usecase.LoginWithFacebookUseCase
import praksa.unravel.talksy.domain.usecase.LoginWithGoogleUseCase
import praksa.unravel.talksy.domain.usecase.ForgotPasswordUseCase
import praksa.unravel.talksy.domain.usecase.GetUserInfoUseCase
import praksa.unravel.talksy.domain.usecase.UpdateUserInfoUseCase
import praksa.unravel.talksy.domain.usecase.UploadProfilePictureUseCase
import praksa.unravel.talksy.domain.usecases.DeleteUserFromAuthUseCase
import praksa.unravel.talksy.main.data.repositories.DirectMessageRepository
import praksa.unravel.talksy.main.domain.usecase.DeleteMessageUseCase
import praksa.unravel.talksy.main.domain.usecase.FetchImageMessagesUseCase
import praksa.unravel.talksy.main.domain.usecase.MarkMessagesAsSeenUseCase

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
    @Provides
    fun provideLoginWithFacebookUseCase(authRepository: AuthRepository): LoginWithFacebookUseCase {
        return LoginWithFacebookUseCase(authRepository)
    }

    @Provides
    fun provideLoginWithGoogleUseCase(authRepository: AuthRepository): LoginWithGoogleUseCase {
        return LoginWithGoogleUseCase(authRepository)
    }

    @Provides
    fun provideLoginUserUseCase(authRepository: AuthRepository):LoginUserUseCase{
        return LoginUserUseCase(authRepository)
    }

    @Provides
    fun provideForgotPasswordUseCase(authRepository: AuthRepository):ForgotPasswordUseCase{
        return ForgotPasswordUseCase(authRepository)
    }

    @Provides
    fun provideCheckPhoneNumberUseCase(authRepository: AuthRepository):CheckPhoneNumberExistsUseCase{
        return CheckPhoneNumberExistsUseCase(authRepository)
    }

    @Provides
    fun provideDeleteUserFromAuthUseCase(authRepository: AuthRepository):DeleteUserFromAuthUseCase{
        return DeleteUserFromAuthUseCase(authRepository)
    }

    @Provides
    fun provideGetUserInfoUseCase(authRepository: AuthRepository): GetUserInfoUseCase {
        return GetUserInfoUseCase(authRepository)
    }

    @Provides
    fun provideUpdateUserInfoUseCase(authRepository: AuthRepository): UpdateUserInfoUseCase {
        return UpdateUserInfoUseCase(authRepository)
    }

    @Provides
    fun provideUploadProfilePictureUseCase(authRepository: AuthRepository): UploadProfilePictureUseCase {
        return UploadProfilePictureUseCase(authRepository)
    }

    @Provides
    fun provideMarkMessagesAsSeenUseCase(repository: DirectMessageRepository):MarkMessagesAsSeenUseCase {
        return MarkMessagesAsSeenUseCase(repository)
    }


//    @Provides
//    fun provideFetchUserFcmTokenUseCase(notRepository: NotificationRepository):FetchUserFcmTokenUseCase{
//        return FetchUserFcmTokenUseCase(notRepository)
//    }
//
//   @Provides
//   fun provideSaveUserFcmTokenUseCase(notRepository: NotificationRepository): SaveUserFcmTokenUseCase {
//       return SaveUserFcmTokenUseCase(notRepository)
//   }
//
//    @Provides
//    fun provideSendNotificationToUserUseCase(notRepository: NotificationRepository): SendNotificationToUserUseCase{
//        return SendNotificationToUserUseCase(notRepository)
//    }
//
//    @Provides
//    @Singleton
//    fun provideNotificationRepository(
//        firestore: FirebaseFirestore
//    ): NotificationRepository {
//        return NotificationRepository(firestore)
//    }

    @Provides
    fun provideDeleteMessageUseCase(repository: DirectMessageRepository): DeleteMessageUseCase {
        return DeleteMessageUseCase(repository)
    }
    @Provides
    fun provideFetchImageMessagesUseCase(
        directMessageRepository: DirectMessageRepository
    ): FetchImageMessagesUseCase {
        return FetchImageMessagesUseCase(directMessageRepository)
    }


}
