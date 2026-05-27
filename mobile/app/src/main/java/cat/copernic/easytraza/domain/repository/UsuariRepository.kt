package cat.copernic.easytraza.domain.repository

import cat.copernic.easytraza.model.Usuari

interface UsuariRepository {
    suspend fun getUsuaris(): List<Usuari>
    suspend fun login(usuariId: Long): Usuari?
}
