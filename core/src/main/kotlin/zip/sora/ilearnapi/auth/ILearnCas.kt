package zip.sora.ilearnapi.auth

import kotlinx.serialization.Serializable
import zip.sora.ilearnapi.get
import zip.sora.ilearnapi.getCookie
import zip.sora.ilearnapi.getJsonp
import zip.sora.ilearnapi.getRedirect
import java.net.URLEncoder
import java.util.Base64

private const val ILEARN_CAS_URL = "https://ilearn.jlu.edu.cn/cas-server/login?"

private fun getILearnCasUrl(params: Map<String, String> = mapOf()) =
    ILEARN_CAS_URL + (params.map {
        "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}"
    }.joinToString("&"))

internal class ILearnCas(private val jwc: Jwc) {

    @Serializable
    private data class GetLtResponse(val lt: String, val execution: String)

    private lateinit var casTgc: String

    private suspend fun getCasTgc() = if (::casTgc.isInitialized) casTgc else refreshCasTgc()

    private suspend fun refreshCasTgc() = jwc.getCredentials().let { (username, password) ->
        getILearnCasUrl().let {
            get(it).getCookie("JSESSIONID")
        }.let { jSessionId ->
            val getLtResp: GetLtResponse = getILearnCasUrl(
                mapOf(
                    "service" to "https://ilearntec.jlu.edu.cn/",
                    "get-lt" to "true"
                )
            ).let {
                get(it, mapOf("JSESSIONID" to jSessionId)).getJsonp()
            }

            getILearnCasUrl(
                mapOf(
                    "service" to "https://ilearntec.jlu.edu.cn/",
                    "username" to username,
                    "password" to Base64.getEncoder().encodeToString(password.toByteArray()),
                    "isajax" to "true",
                    "_eventId" to "submit",
                    "lt" to getLtResp.lt,
                    "execution" to getLtResp.execution
                )
            ).let {
                get(it, mapOf("JSESSIONID" to jSessionId)).getCookie("CASTGC")
            }
        }
    }.also { casTgc = it }

    suspend fun authenticate(service: String) =
        getILearnCasUrl(mapOf("service" to service)).let {
            get(it, mapOf("CASTGC" to getCasTgc())).getRedirect()
        }
}

internal fun Jwc.authenticateILearnCas() = ILearnCas(this)