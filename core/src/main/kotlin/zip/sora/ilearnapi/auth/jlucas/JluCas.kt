package zip.sora.ilearnapi.auth.jlucas

import zip.sora.ilearnapi.get
import zip.sora.ilearnapi.getCookie
import zip.sora.ilearnapi.getElemValue
import zip.sora.ilearnapi.getRedirect
import zip.sora.ilearnapi.post
import zip.sora.ilearnapi.urlEncodeString

private const val JLUCAS_URL = "https://cas.jlu.edu.cn/tpass/login?service="

class CasAuthenticationException : Exception()

internal class JluCas(
    private val username: String, private val password: String
) {
    suspend fun authenticate(service: String): String {
        val url = JLUCAS_URL + urlEncodeString(service)
        val resp = get(url).let {
            val lt = it.getElemValue("#lt")
            post(
                url, mapOf(
                    "rsa" to strEnc(username + password + lt),
                    "ul" to username.length.toString(),
                    "pl" to password.length.toString(),
                    "sl" to "0",
                    "lt" to lt,
                    "execution" to "e1s1",
                    "_eventId" to "submit"
                ), mapOf(
                    "tpasssessionid" to it.getCookie("tpasssessionid")
                )
            )
        }

        try {
            return resp.getRedirect()
        } catch (e: IllegalStateException) {
            throw CasAuthenticationException()
        }
    }
}
