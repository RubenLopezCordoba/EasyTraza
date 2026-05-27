package cat.copernic.easytraza.domain.repository

import cat.copernic.easytraza.model.Lot

interface LotRepository {
    suspend fun getLots(): List<Lot>
    suspend fun updateLotEstat(nifProveidor: String, idLot: String, estat: String)
}
