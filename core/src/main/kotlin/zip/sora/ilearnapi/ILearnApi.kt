package zip.sora.ilearnapi

import zip.sora.ilearnapi.auth.authenticateILearnCas
import zip.sora.ilearnapi.auth.authenticateJwc
import zip.sora.ilearnapi.auth.jlucas.JluCas
import zip.sora.ilearnapi.service.ilearn.res.authenticateILearnRes
import zip.sora.ilearnapi.service.ilearn.tec.authenticateILearnTec

class ILearnApi(
    username: String,
    password: String,
    onTecSession: (String) -> Unit,
    onResSession: (String) -> Unit
) {
    private val ilearnCas = JluCas(username, password).authenticateJwc().authenticateILearnCas()
    val tecService = ilearnCas.authenticateILearnTec(onTecSession)
    val resService = ilearnCas.authenticateILearnRes(onResSession)
}