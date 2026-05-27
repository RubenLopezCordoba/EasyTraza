package cat.copernic.easytraza.data.repository

import cat.copernic.easytraza.api.LotApi
import cat.copernic.easytraza.domain.repository.LotRepository
import cat.copernic.easytraza.model.Lot
import cat.copernic.easytraza.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LotRepositoryImpl(private val ip: String) : LotRepository {

    private val api = RetrofitClient.getClient(ip).create(LotApi::class.java)

    override suspend fun getLots(): List<Lot> = withContext(Dispatchers.IO) {
        val response = api.getLots().execute()
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw Exception("Error (${response.code()})")
    }

    override suspend fun updateLotEstat(nifProveidor: String, idLot: String, estat: String) {
        withContext(Dispatchers.IO) {
            val response = when (estat) {
                "OBERT" -> api.iniciarLot(nifProveidor, idLot).execute()
                "ACABAT" -> api.finalitzarLot(nifProveidor, idLot).execute()
                else -> throw Exception("Estat no vàlid: $estat")
            }
            if (!response.isSuccessful) {
                throw Exception("Error (${response.code()})")
            }
        }
    }
}
