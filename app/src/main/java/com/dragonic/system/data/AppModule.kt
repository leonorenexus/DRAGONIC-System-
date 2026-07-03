package com.dragonic.system.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.dragonic.system.data.db.DragonicDatabase
import com.dragonic.system.data.model.IntruderLogDao
import com.dragonic.system.data.model.OwnerFaceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dragonic_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DragonicDatabase {
        return Room.databaseBuilder(
            context,
            DragonicDatabase::class.java,
            "dragonic_db"
        ).build()
    }

    @Provides
    fun provideIntruderLogDao(db: DragonicDatabase): IntruderLogDao = db.intruderLogDao()

    @Provides
    fun provideOwnerFaceDao(db: DragonicDatabase): OwnerFaceDao = db.ownerFaceDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}
