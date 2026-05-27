package cat.copernic.easytraza.data.repository

import cat.copernic.easytraza.api.AlbaraApi
import cat.copernic.easytraza.domain.repository.AlbaraRepository
import cat.copernic.easytraza.model.AlbarraProveidor
import cat.copernic.easytraza.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlbaraRepositoryImpl(private val ip: String) : AlbaraRepository {

    private val api = RetrofitClient.getClient(ip).create(AlbaraApi::class.java)

    override suspend fun getAlbarans(): List<AlbarraProveidor> = withContext(Dispatchers.IO) {
        val response = api.getAlbarans().execute()
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw Exception("Error (${response.code()})")
    }
}
