package cat.copernic.easytraza.domain.usecase

import cat.copernic.easytraza.domain.repository.AlbaraRepository
import cat.copernic.easytraza.model.AlbarraProveidor

class GetAlbaransUseCase(private val repository: AlbaraRepository) {
    suspend operator fun invoke(): Result<List<AlbarraProveidor>> = runCatching {
        repository.getAlbarans()
    }
}
