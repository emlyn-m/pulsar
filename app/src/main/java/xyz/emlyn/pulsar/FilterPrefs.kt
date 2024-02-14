package xyz.emlyn.pulsar

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FilterPrefs(private val context : Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "filterPrefs")
        val SHOW_DISMISSED_ALERTS = booleanPreferencesKey("show_dismissed")
        val MIN_SEV = intPreferencesKey("min_sev")
    }

    val getShowDismissed : Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_DISMISSED_ALERTS] ?: false
    }

    val getMinSev : Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[MIN_SEV] ?: 6
    }

    suspend fun setShowDismissed(value : Boolean) {
      context.dataStore.edit { it[SHOW_DISMISSED_ALERTS] = value }
    }

    suspend fun setMinSev(minSev : Int) {
        context.dataStore.edit { it[MIN_SEV] = minSev }
    }
}