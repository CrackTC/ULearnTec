package zip.sora.ulearntec.domain

import android.content.Context

sealed class ILearnResult<T>(
    val data: T? = null,
    val error: ((Context) -> String)?
) {
    class Success<T>(data: T) : ILearnResult<T>(data, null)
    class Error<T>(error: ((Context) -> String)?) : ILearnResult<T>(null, error)
}