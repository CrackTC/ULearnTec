package zip.sora.ilearnapi.service.ilearn

import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import zip.sora.ilearnapi.auth.ILearnCas
import zip.sora.ilearnapi.getCookie
import zip.sora.ilearnapi.get as utilGet

val json = Json { ignoreUnknownKeys = true }

abstract class ILearnService internal constructor(
    private val host: String,
    private val casPath: String,
    private val cas: ILearnCas,
    private val sessionCookieName: String,
    private val onSessionFetched: (String) -> Unit,
) {
    @Serializable
    private data class ApiResult(
        val code: String? = null,
        val status: String,
        val message: String,
        val location: String?,
        val data: JsonElement
    )

    private lateinit var session: String

    private suspend fun refreshSession() = cas.authenticate(host + casPath).let {
        utilGet(it).getCookie(sessionCookieName)
    }.also(onSessionFetched).also { session = it }

    fun setSession(session: String) {
        this.session = session
    }

    private suspend fun getSession() =
        if (::session.isInitialized) session
        else refreshSession()

    private suspend fun getRaw(endpoint: String) =
        utilGet(host + endpoint, mapOf(sessionCookieName to getSession())).bodyAsText()

    protected suspend fun <T> get(
        deserializer: DeserializationStrategy<T>,
        endpoint: String,
        retry: Int = 1
    ): T = getRaw(endpoint).let {
        val result: ApiResult = json.decodeFromString(it)
        if (result.code == "5101" || result.status == "0") {
            if (retry > 0) {
                refreshSession()
                get(deserializer, endpoint, retry - 1)
            } else {
                throw Exception("API get failed after max retry: $it")
            }
        } else {
            json.decodeFromJsonElement(deserializer, result.data)
        }
    }

    protected suspend inline fun <reified T> get(endpoint: String): T =
        get(json.serializersModule.serializer(), endpoint)
}