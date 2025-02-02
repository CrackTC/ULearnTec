package zip.sora.ulearntec.domain

import android.content.Context
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed interface ILearnResult<T> {
    data class Success<T>(val data: T) : ILearnResult<T>
    data class Error<T>(val error: ((Context) -> String)) : ILearnResult<T>
}

@OptIn(ExperimentalContracts::class)
fun <T> ILearnResult<T>.isError(): Boolean {
    contract {
        returns(true) implies (this@isError is ILearnResult.Error)
        returns(false) implies (this@isError is ILearnResult.Success)
    }
    return this is ILearnResult.Error
}