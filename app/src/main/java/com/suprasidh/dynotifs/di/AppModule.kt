package com.suprasidh.dynotifs.di

import android.app.NotificationManager
import android.content.Context
import com.suprasidh.dynotifs.data.datastore.DynotifsDataStore
import com.suprasidh.dynotifs.domain.queue.PriorityNotificationQueue
import com.suprasidh.dynotifs.overlay.IslandStateMachine
import com.suprasidh.dynotifs.overlay.OverlayWindowManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDynotifsDataStore(
        @ApplicationContext context: Context
    ): DynotifsDataStore = DynotifsDataStore(context)

    @Provides
    @Singleton
    fun providePriorityNotificationQueue(): PriorityNotificationQueue = PriorityNotificationQueue()

    @Provides
    @Singleton
    fun provideOverlayWindowManager(
        @ApplicationContext context: Context,
        dataStore: DynotifsDataStore
    ): OverlayWindowManager = OverlayWindowManager(context, dataStore)

    @Provides
    @Singleton
    fun provideIslandStateMachine(
        queue: PriorityNotificationQueue,
        dataStore: DynotifsDataStore,
        overlayWindowManager: OverlayWindowManager
    ): IslandStateMachine = IslandStateMachine(queue, dataStore, overlayWindowManager)

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}