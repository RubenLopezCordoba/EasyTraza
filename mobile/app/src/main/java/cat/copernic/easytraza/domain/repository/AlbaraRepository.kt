package cat.copernic.easytraza.domain.repository

import cat.copernic.easytraza.model.AlbarraProveidor

interface AlbaraRepository {
    suspend fun getAlbarans(): List<AlbarraProveidor>
}
