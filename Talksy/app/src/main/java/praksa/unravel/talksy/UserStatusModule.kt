package praksa.unravel.talksy

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import praksa.unravel.talksy.main.data.repositories.UserStatusRepository
import praksa.unravel.talksy.main.domain.UserStatusUsecase.GetUserStatusUseCase
import praksa.unravel.talksy.main.domain.UserStatusUsecase.SetUserOfflineUseCase
import praksa.unravel.talksy.main.domain.UserStatusUsecase.SetUserOnlineUseCase
import praksa.unravel.talksy.model.User
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserStatusModule {

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance()
    }

    @Provides
    @Singleton
    fun provideUserStatusRepository(
         auth: FirebaseAuth,
         database: FirebaseDatabase
    ):UserStatusRepository{
        return UserStatusRepository(auth,database)
    }

    @Provides
    fun provideGetUserStatusUseCase(repository: UserStatusRepository):GetUserStatusUseCase{
        return GetUserStatusUseCase(repository)
    }

    @Provides
    fun provideSetUserOfflineUseCase(repository: UserStatusRepository):SetUserOfflineUseCase{
        return SetUserOfflineUseCase(repository)
    }

    @Provides
    fun provdeSetUserOnlineUseCase(repository: UserStatusRepository):SetUserOnlineUseCase{
        return SetUserOnlineUseCase(repository)
    }

}