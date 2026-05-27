package cat.copernic.easytraza.domain.usecase

import cat.copernic.easytraza.domain.repository.UsuariRepository
import cat.copernic.easytraza.model.Usuari

class LoginUseCase(private val repository: UsuariRepository) {
    suspend operator fun invoke(usuariId: Long): Result<Usuari?> = runCatching {
        repository.login(usuariId)
    }
}
