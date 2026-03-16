package com.ofppt.istak.di

import com.ofppt.istak.data.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.firstOrNull

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(tokenManager: com.ofppt.istak.data.local.TokenManager): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://absence.ofppt.dev/api/") // URL de production (Live)
            .client(
                okhttp3.OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val original = chain.request()
                        val requestBuilder = original.newBuilder()
                            .addHeader("Accept", "application/json")
                            .addHeader("X-Requested-With", "XMLHttpRequest")

                        // Add Token if exists
                        val token = runBlocking {
                            tokenManager.token.firstOrNull()
                        }

                        if (!token.isNullOrEmpty()) {
                            requestBuilder.addHeader("Authorization", "Bearer $token")
                        }

                        chain.proceed(requestBuilder.build())
                    }
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): com.ofppt.istak.data.local.AppDatabase {
        return androidx.room.Room.databaseBuilder(
            context,
            com.ofppt.istak.data.local.AppDatabase::class.java,
            "istak_db"
        ).build()
    }

    @Provides
    fun provideUserDao(database: com.ofppt.istak.data.local.AppDatabase): com.ofppt.istak.data.local.UserDao {
        return database.userDao()
    }
}
