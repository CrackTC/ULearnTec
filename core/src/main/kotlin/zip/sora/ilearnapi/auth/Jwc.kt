package zip.sora.ilearnapi.auth

import zip.sora.ilearnapi.auth.jlucas.JluCas
import zip.sora.ilearnapi.get
import zip.sora.ilearnapi.getCookie
import zip.sora.ilearnapi.getElemValue

private const val JWC_URL =
    "https://jwcidentity.jlu.edu.cn/iplat-pass-jlu/thirdLogin/jlu/login"

internal class Jwc(private val cas: JluCas) {
    suspend fun getCredentials() = cas.authenticate(JWC_URL).let {
        get(it).getCookie("JSESSIONID")
    }.let {
        get(JWC_URL, mapOf("JSESSIONID" to it))
    }.let {
        it.getElemValue("#username") to it.getElemValue("#password")
    }
}

internal fun JluCas.authenticateJwc() = Jwc(this)