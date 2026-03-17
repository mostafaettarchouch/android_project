package com.ofppt.istak.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val SCHOOL_YEAR_START_KEY = stringPreferencesKey("school_year_start")
        private val SCHOOL_YEAR_END_KEY = stringPreferencesKey("school_year_end")
        private val LAST_NEWS_ID_KEY = androidx.datastore.preferences.core.intPreferencesKey("last_news_id")
        
        // Contact Info
        private val CONTACT_PHONE_KEY = stringPreferencesKey("contact_phone")
        private val CONTACT_EMAIL_KEY = stringPreferencesKey("contact_email")
        private val CONTACT_FACEBOOK_KEY = stringPreferencesKey("contact_facebook")
        private val CONTACT_INSTAGRAM_KEY = stringPreferencesKey("contact_instagram")
        private val CONTACT_WHATSAPP_KEY = stringPreferencesKey("contact_whatsapp")
        private val DARK_MODE_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("dark_mode")
    }

    val isDarkMode: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY]
    }

    suspend fun saveDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDark
        }
    }

    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    val schoolYearStart: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SCHOOL_YEAR_START_KEY]
    }

    val schoolYearEnd: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SCHOOL_YEAR_END_KEY]
    }
    
    val contactInfo: Flow<Map<String, String?>> = context.dataStore.data.map { preferences ->
        mapOf(
            "phone" to preferences[CONTACT_PHONE_KEY],
            "email" to preferences[CONTACT_EMAIL_KEY],
            "facebook" to preferences[CONTACT_FACEBOOK_KEY],
            "instagram" to preferences[CONTACT_INSTAGRAM_KEY],
            "whatsapp" to preferences[CONTACT_WHATSAPP_KEY]
        )
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveSchoolYear(start: String, end: String) {
        context.dataStore.edit { preferences ->
            preferences[SCHOOL_YEAR_START_KEY] = start
            preferences[SCHOOL_YEAR_END_KEY] = end
        }
    }
    
    suspend fun saveContactInfo(phone: String?, email: String?, facebook: String?, instagram: String?, whatsapp: String?) {
        context.dataStore.edit { preferences ->
            if (phone != null) preferences[CONTACT_PHONE_KEY] = phone else preferences.remove(CONTACT_PHONE_KEY)
            if (email != null) preferences[CONTACT_EMAIL_KEY] = email else preferences.remove(CONTACT_EMAIL_KEY)
            if (facebook != null) preferences[CONTACT_FACEBOOK_KEY] = facebook else preferences.remove(CONTACT_FACEBOOK_KEY)
            if (instagram != null) preferences[CONTACT_INSTAGRAM_KEY] = instagram else preferences.remove(CONTACT_INSTAGRAM_KEY)
            if (whatsapp != null) preferences[CONTACT_WHATSAPP_KEY] = whatsapp else preferences.remove(CONTACT_WHATSAPP_KEY)
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }



    val lastNewsId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[LAST_NEWS_ID_KEY]
    }

    suspend fun saveLastNewsId(id: Int) {
        context.dataStore.edit { preferences ->
            preferences[LAST_NEWS_ID_KEY] = id
        }
    }
}
