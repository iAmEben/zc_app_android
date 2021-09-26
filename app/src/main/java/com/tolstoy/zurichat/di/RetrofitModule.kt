package com.tolstoy.zurichat.di

import com.google.gson.Gson
import com.tolstoy.zurichat.data.remoteSource.ChatsService
import com.tolstoy.zurichat.data.remoteSource.FilesService
import com.tolstoy.zurichat.data.remoteSource.Retrofit as RetrofitBuilder
import com.tolstoy.zurichat.data.remoteSource.UsersService
import com.tolstoy.zurichat.data.remoteSource.RoomService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    fun provideGson(): Gson {
        return Gson().newBuilder().setLenient().create()
    }

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().also {
            it.level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    fun provideClient(interceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor(interceptor).build()
    }

    @Provides
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.zuri.chat/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    fun provideRetrofitService(retrofit: Retrofit): UsersService =
        retrofit.create(UsersService::class.java)

    @Provides
    fun provideChatService() =
        RetrofitBuilder.retrofit(ChatsService.BASE_URL).create(ChatsService::class.java)

    @Provides
    fun provideRoomService() =
        RetrofitBuilder.retrofit(RoomService.BASE_URL).create(RoomService::class.java)

    @Provides
    fun provideFileService() =
        RetrofitBuilder.retrofit(FilesService.BASE_URL).create(FilesService::class.java)
}