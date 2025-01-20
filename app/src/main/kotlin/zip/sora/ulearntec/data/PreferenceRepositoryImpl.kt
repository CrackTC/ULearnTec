package zip.sora.ulearntec.data

import android.content.Context
import android.text.format.DateUtils.DAY_IN_MILLIS
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import zip.sora.ulearntec.R
import zip.sora.ulearntec.domain.model.Credential
import zip.sora.ulearntec.domain.ILearnResult
import zip.sora.ulearntec.domain.PreferenceRepository
import java.time.Instant

private const val PREFERENCES_NAME = "ilearn_preferences"

private object PreferencesKeys {
    val LOGGED_IN = booleanPreferencesKey("logged_in")
    val USERNAME = stringPreferencesKey("username")
    val PASSWORD = stringPreferencesKey("password")
    val REFRESH_INTERVAL_MILLIS = longPreferencesKey("refresh_interval_millis")
    val TEC_SESSION = stringPreferencesKey("tec_session")
    val RES_SESSION = stringPreferencesKey("res_session")
}

private val Context.dataStore by preferencesDataStore(name = PREFERENCES_NAME)

class PreferenceRepositoryImpl(context: Context) : PreferenceRepository {
    private val dataStore = context.dataStore

    override suspend fun getTecSession(): String? =
        dataStore.data.first()[PreferencesKeys.TEC_SESSION]

    override suspend fun setTecSession(session: String) {
        dataStore.edit { it[PreferencesKeys.TEC_SESSION] = session }
    }

    override suspend fun getResSession(): String? =
        dataStore.data.first()[PreferencesKeys.RES_SESSION]

    override suspend fun setResSession(session: String) {
        dataStore.edit { it[PreferencesKeys.RES_SESSION] = session }
    }

    override suspend fun clearSession() {
        dataStore.edit {
            it.remove(PreferencesKeys.TEC_SESSION)
            it.remove(PreferencesKeys.RES_SESSION)
        }
    }

    override suspend fun isOutOfDate(lastUpdated: Long): Boolean {
        val millis =
            dataStore.data.first()[PreferencesKeys.REFRESH_INTERVAL_MILLIS] ?: DAY_IN_MILLIS
        return lastUpdated + millis < Instant.now().toEpochMilli()
    }

    override suspend fun setRefreshIntervalMillis(millis: Long) {
        dataStore.edit { it[PreferencesKeys.REFRESH_INTERVAL_MILLIS] = millis }
    }

    override suspend fun clearRefreshIntervalMillis() {
        dataStore.edit { it.remove(PreferencesKeys.REFRESH_INTERVAL_MILLIS) }
    }

    override suspend fun isLoggedIn(): Boolean =
        dataStore.data.first()[PreferencesKeys.LOGGED_IN] ?: false

    override suspend fun setLoggedIn(loggedIn: Boolean) {
        dataStore.edit { it[PreferencesKeys.LOGGED_IN] = loggedIn }
    }

    override suspend fun getCredential(): ILearnResult<Credential> {
        val preferences = dataStore.data.first()
        val username = preferences[PreferencesKeys.USERNAME]
        val password = preferences[PreferencesKeys.PASSWORD]
        if (username == null || password == null) {
            return ILearnResult.Error { it.getString(R.string.username_or_password_not_set) }
        }
        return ILearnResult.Success(Credential(username, password))
    }

    override suspend fun updateCredential(credential: Credential) {
        dataStore.edit {
            it[PreferencesKeys.USERNAME] = credential.username
            it[PreferencesKeys.PASSWORD] = credential.password
        }
    }

    override suspend fun clearCredential() {
        dataStore.edit {
            it.remove(PreferencesKeys.USERNAME)
            it.remove(PreferencesKeys.PASSWORD)
        }
    }
}