package com.rony.media3tutorial

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.rony.media3tutorial.service.MediaPlaybackService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(ViewModelComponent::class)
object VideoMetaDataModule {

    @Provides
    @ViewModelScoped
    fun provideMetaDataReader(app: Application): MetaDataReader {
        return MetaDataReaderImpl(app)
    }
}

@Module
@InstallIn(ServiceComponent::class)
object VideoPlayerModules {

    @Provides
    @ServiceScoped
    fun provideExoPlayer(
        @ApplicationContext context: Context
    ): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(5_000)
            .setSeekForwardIncrementMs(5_000)
            .build()
    }
}

@OptIn(UnstableApi::class)
@Module
@InstallIn(SingletonComponent::class)
object MediaSessionModule {


    @Provides
    @Singleton
    fun provideSessionToken(
        @ApplicationContext context: Context
    ): SessionToken {
        return SessionToken(
            context,
            ComponentName(context, MediaPlaybackService::class.java)
        )
    }
}