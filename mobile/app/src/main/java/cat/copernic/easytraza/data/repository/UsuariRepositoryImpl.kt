package cat.copernic.easytraza.data.repository

import cat.copernic.easytraza.api.LoginApi
import cat.copernic.easytraza.domain.repository.UsuariRepository
import cat.copernic.easytraza.model.Usuari
import cat.copernic.easytraza.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsuariRepositoryImpl(private val ip: String) : UsuariRepository {

    private val api = RetrofitClient.getClient(ip).create(LoginApi::class.java)

    override suspend fun getUsuaris(): List<Usuari> = withContext(Dispatchers.IO) {
        val response = api.getUsuarios().execute()
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw Exception("Error del servidor (${response.code()})")
    }

    override suspend fun login(usuariId: Long): Usuari? = withContext(Dispatchers.IO) {
        val response = api.login(mapOf("usuarioId" to usuariId)).execute()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.success) body.usuari
            else throw Exception(body?.error ?: "Error al iniciar sessió")
        } else throw Exception("Error del servidor (${response.code()})")
    }
}
