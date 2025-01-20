package zip.sora.ilearnapi

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.cookie
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.parameters
import io.ktor.http.setCookie
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

internal fun urlEncodeString(input: String): String {
    return URLEncoder.encode(input, "UTF-8")
}

internal fun HttpResponse.getCookie(key: String) = setCookie().first { it.name == key }.value

internal suspend fun HttpResponse.getElemValue(selector: String) = Jsoup.parse(bodyAsText()).let {
    it.selectFirst(selector)?.`val`() ?: throw Exception("Element not found: $selector")
}

private val jsonpRegex = """(?<=\().*(?=\))""".toRegex()
internal suspend inline fun <reified T> HttpResponse.getJsonp(): T =
    jsonpRegex.find(bodyAsText())?.let { Json.decodeFromString(it.value) }
        ?: throw Exception("No JSONP content")

internal fun HttpResponse.getRedirect() =
    headers["Location"] ?: throw IllegalStateException("No Location header")

private val ilearnTrustManager by lazy {
    val trustStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
        load(null)
        object {}.javaClass.getResourceAsStream("/RapidSSL.cer")!!.use {
            CertificateFactory.getInstance("X.509").generateCertificate(it)
        }.let {
            setCertificateEntry("RapidSSL", it)
        }
    }
    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())!!
        .also { it.init(trustStore) }
        .let { it.trustManagers!!.filterIsInstance<X509TrustManager>().first() }
}

fun createILearnHttpClient(followRedirects: Boolean = true): HttpClient {
    return HttpClient(CIO) {
        this.followRedirects = followRedirects
        engine {
            https {
                trustManager = ilearnTrustManager
            }
        }
    }
}

private val client = createILearnHttpClient(followRedirects = false)

internal suspend fun get(url: String, cookies: Map<String, String> = mapOf()) = client.get(url) {
    // workaround for that CASTGC cannot be urlencoded
    cookies.map { (key, value) -> "$key=$value" }.joinToString("; ").let {
        headers.append("Cookie", it)
    }
}

internal suspend fun post(
    url: String,
    body: Map<String, String>,
    cookies: Map<String, String> = mapOf()
) = client.submitForm(
    url, parameters {
        body.forEach { (key, value) -> append(key, value) }
    }) {
    cookies.forEach { (key, value) -> cookie(key, value) }
}