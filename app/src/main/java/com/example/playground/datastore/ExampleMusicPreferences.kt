package com.example.playground.datastore

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class ExampleMusicPreferences(context: Context) {

    private val dataStore: DataStore<Preferences> = context.createDataStore(
        name = "music_pref"
    )

    companion object{
        val LAST_PLAYED_SONG_KEY = preferencesKey<Int>(name = "last_Played_song")
    }

    suspend fun saveLastPlayedSong(id: Int){
        dataStore.edit { preferences ->
            preferences[LAST_PLAYED_SONG_KEY] = id
        }
    }

    val lastPlayedSong: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[LAST_PLAYED_SONG_KEY] ?: -1
        }
}