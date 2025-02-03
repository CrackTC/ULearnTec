package zip.sora.ulearntec.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import zip.sora.ulearntec.DEFAULT_DATA_EXPIRE_MILLIS
import zip.sora.ulearntec.DEFAULT_LONG_PRESS_SPEED
import zip.sora.ulearntec.DEFAULT_MAX_PLAYER_CACHE_MB
import zip.sora.ulearntec.DEFAULT_SWIPE_BRIGHTNESS_PERCENT
import zip.sora.ulearntec.DEFAULT_SWIPE_SEEK_FIXED_MILLIS
import zip.sora.ulearntec.DEFAULT_SWIPE_SEEK_PERCENT
import zip.sora.ulearntec.DEFAULT_SWIPE_VOLUME_PERCENT
import zip.sora.ulearntec.R
import zip.sora.ulearntec.domain.Theme
import zip.sora.ulearntec.domain.model.Credential
import zip.sora.ulearntec.domain.ILearnResult
import zip.sora.ulearntec.domain.PlayerTheme
import zip.sora.ulearntec.domain.PreferenceRepository
import zip.sora.ulearntec.domain.SwipeSeekMode
import java.time.Instant

private const val PREFERENCES_NAME = "ilearn_preferences"

private object PreferencesKeys {
    // opaque
    val LOGGED_IN = booleanPreferencesKey("logged_in")
    val USERNAME = stringPreferencesKey("username")
    val PASSWORD = stringPreferencesKey("password")
    val TEC_SESSION = stringPreferencesKey("tec_session")
    val RES_SESSION = stringPreferencesKey("res_session")

    // network
    val DATA_EXPIRE_MILLIS = longPreferencesKey("refresh_interval_millis")

    // storage
    val MAX_PLAYER_CACHE_MB = longPreferencesKey("max_player_cache_mb")

    // appearance
    val THEME = intPreferencesKey("theme")
    val PLAYER_THEME = intPreferencesKey("player_theme")

    // player
    val SWIPE_SEEK_MODE = intPreferencesKey("swipe_seek_mode")
    val SWIPE_SEEK_FIXED_MILLIS = longPreferencesKey("swipe_seek_fixed_millis")
    val SWIPE_SEEK_PERCENT = floatPreferencesKey("swipe_seek_percent")

    val SWIPE_VOLUME_PERCENT = floatPreferencesKey("swipe_volume_percent")
    val SWIPE_BRIGHTNESS_PERCENT = floatPreferencesKey("swipe_brightness_percent")

    val LONG_PRESS_SPEED = floatPreferencesKey("long_press_speed")
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
        val millis = getDataExpireMillis()
        if (millis == 0L) return false

        return lastUpdated + millis < Instant.now().toEpochMilli()
    }

    override suspend fun getDataExpireMillis() =
        dataStore.data.first()[PreferencesKeys.DATA_EXPIRE_MILLIS] ?: DEFAULT_DATA_EXPIRE_MILLIS

    override suspend fun setDataExpireMillis(millis: Long) {
        dataStore.edit { it[PreferencesKeys.DATA_EXPIRE_MILLIS] = millis }
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

    override suspend fun getMaxPlayerCacheMb() =
        dataStore.data.first()[PreferencesKeys.MAX_PLAYER_CACHE_MB] ?: DEFAULT_MAX_PLAYER_CACHE_MB

    override suspend fun setMaxPlayerCacheMb(mb: Long) {
        dataStore.edit { it[PreferencesKeys.MAX_PLAYER_CACHE_MB] = mb }
    }

    override suspend fun getTheme() =
        dataStore.data.first()[PreferencesKeys.THEME]?.let { value ->
            Theme.entries.first { it.value == value }
        } ?: Theme.SYSTEM

    override suspend fun setTheme(theme: Theme) {
        dataStore.edit { it[PreferencesKeys.THEME] = theme.value }
    }

    override suspend fun getPlayerTheme() =
        dataStore.data.first()[PreferencesKeys.PLAYER_THEME]?.let { value ->
            PlayerTheme.entries.first { it.value == value }
        } ?: PlayerTheme.FOLLOW_THEME

    override suspend fun setPlayerTheme(theme: PlayerTheme) {
        dataStore.edit { it[PreferencesKeys.PLAYER_THEME] = theme.value }
    }

    override suspend fun getSwipeSeekMode() =
        dataStore.data.first()[PreferencesKeys.SWIPE_SEEK_MODE]?.let { value ->
            SwipeSeekMode.entries.first { it.value == value }
        } ?: SwipeSeekMode.FIXED

    override suspend fun setSwipeSeekMode(mode: SwipeSeekMode) {
        dataStore.edit { it[PreferencesKeys.SWIPE_SEEK_MODE] = mode.value }
    }

    override suspend fun getSwipeSeekFixedMillis() =
        dataStore.data.first()[PreferencesKeys.SWIPE_SEEK_FIXED_MILLIS]
            ?: DEFAULT_SWIPE_SEEK_FIXED_MILLIS

    override suspend fun setSwipeSeekFixedMillis(millis: Long) {
        dataStore.edit { it[PreferencesKeys.SWIPE_SEEK_FIXED_MILLIS] = millis }
    }

    override suspend fun getSwipeSeekPercent() =
        dataStore.data.first()[PreferencesKeys.SWIPE_SEEK_PERCENT] ?: DEFAULT_SWIPE_SEEK_PERCENT

    override suspend fun setSwipeSeekPercent(percent: Float) {
        dataStore.edit { it[PreferencesKeys.SWIPE_SEEK_PERCENT] = percent }
    }

    override suspend fun getSwipeVolumePercent() =
        dataStore.data.first()[PreferencesKeys.SWIPE_VOLUME_PERCENT] ?: DEFAULT_SWIPE_VOLUME_PERCENT

    override suspend fun setSwipeVolumePercent(percent: Float) {
        dataStore.edit { it[PreferencesKeys.SWIPE_VOLUME_PERCENT] = percent }
    }

    override suspend fun getSwipeBrightnessPercent() =
        dataStore.data.first()[PreferencesKeys.SWIPE_BRIGHTNESS_PERCENT]
            ?: DEFAULT_SWIPE_BRIGHTNESS_PERCENT

    override suspend fun setSwipeBrightnessPercent(percent: Float) {
        dataStore.edit { it[PreferencesKeys.SWIPE_BRIGHTNESS_PERCENT] = percent }
    }

    override suspend fun getLongPressSpeed() =
        dataStore.data.first()[PreferencesKeys.LONG_PRESS_SPEED] ?: DEFAULT_LONG_PRESS_SPEED

    override suspend fun setLongPressSpeed(speed: Float) {
        dataStore.edit { it[PreferencesKeys.LONG_PRESS_SPEED] = speed }
    }
}