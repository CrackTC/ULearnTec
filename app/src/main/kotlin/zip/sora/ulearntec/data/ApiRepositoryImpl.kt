package zip.sora.ulearntec.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import zip.sora.ilearnapi.ILearnApi
import zip.sora.ulearntec.domain.ApiRepository
import zip.sora.ulearntec.domain.ILearnResult
import zip.sora.ulearntec.domain.PreferenceRepository
import zip.sora.ulearntec.domain.model.Credential

class ApiRepositoryImpl(
    private val preferenceRepository: PreferenceRepository,
) : ApiRepository {

    private var api: ILearnApi? = null

    override suspend fun getApi(): ILearnResult<ILearnApi> =
        api?.let { ILearnResult.Success(api!!) } ?: preferenceRepository.getCredential()
            .let { res ->
                if (res is ILearnResult.Error) ILearnResult.Error(res.error)
                else ILearnResult.Success(
                    ILearnApi(
                        res.data!!.username,
                        res.data.password,
                        { tecSession ->
                            CoroutineScope(Dispatchers.Default).launch {
                                preferenceRepository.setTecSession(tecSession)
                            }
                        },
                        { resSession ->
                            CoroutineScope(Dispatchers.Default).launch {
                                preferenceRepository.setResSession(resSession)
                            }
                        }
                    ).also { api ->
                        this.api = api
                        preferenceRepository.getTecSession()
                            ?.let { api.tecService.setSession(it) }
                        preferenceRepository.getResSession()
                            ?.let { api.resService.setSession(it) }
                    }
                )
            }

    override suspend fun login(username: String, password: String) {
        api = null
        preferenceRepository.clearSession()
        preferenceRepository.updateCredential(Credential(username, password))
    }

    override suspend fun logout() {
        api = null
        preferenceRepository.clearSession()
        preferenceRepository.clearCredential()
    }
}