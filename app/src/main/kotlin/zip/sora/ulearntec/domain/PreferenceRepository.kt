package zip.sora.ulearntec.domain

import zip.sora.ulearntec.domain.model.Credential

enum class Theme(val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2)
}

enum class PlayerTheme(val value: Int) {
    FOLLOW_THEME(-1),
    SYSTEM(0),
    LIGHT(1),
    DARK(2)
}

enum class SwipeSeekMode(val value: Int) {
    FIXED(0),
    PERCENT(1)
}

interface PreferenceRepository {

    suspend fun getTecSession(): String?
    suspend fun setTecSession(session: String)
    suspend fun getResSession(): String?
    suspend fun setResSession(session: String)
    suspend fun clearSession()

    suspend fun isOutOfDate(lastUpdated: Long): Boolean
    suspend fun getDataExpireMillis(): Long
    suspend fun setDataExpireMillis(millis: Long)

    suspend fun isLoggedIn(): Boolean
    suspend fun setLoggedIn(loggedIn: Boolean)

    suspend fun getCredential(): ILearnResult<Credential>
    suspend fun updateCredential(credential: Credential)
    suspend fun clearCredential()

    suspend fun getMaxPlayerCacheMb(): Long
    suspend fun setMaxPlayerCacheMb(mb: Long)

    suspend fun getTheme(): Theme
    suspend fun setTheme(theme: Theme)
    suspend fun getPlayerTheme(): PlayerTheme
    suspend fun setPlayerTheme(theme: PlayerTheme)

    suspend fun getSwipeSeekMode(): SwipeSeekMode
    suspend fun setSwipeSeekMode(mode: SwipeSeekMode)
    suspend fun getSwipeSeekFixedMillis(): Long
    suspend fun setSwipeSeekFixedMillis(millis: Long)
    suspend fun getSwipeSeekPercent(): Float
    suspend fun setSwipeSeekPercent(percent: Float)

    suspend fun getSwipeVolumePercent(): Float
    suspend fun setSwipeVolumePercent(percent: Float)
    suspend fun getSwipeBrightnessPercent(): Float
    suspend fun setSwipeBrightnessPercent(percent: Float)

    suspend fun getLongPressSpeed(): Float
    suspend fun setLongPressSpeed(speed: Float)
}