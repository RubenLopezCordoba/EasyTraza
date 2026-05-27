package cat.copernic.easytraza.domain.usecase

import cat.copernic.easytraza.domain.repository.UsuariRepository
import cat.copernic.easytraza.model.Usuari

class GetUsuarisUseCase(private val repository: UsuariRepository) {
    suspend operator fun invoke(): Result<List<Usuari>> = runCatching {
        repository.getUsuaris()
    }
}
