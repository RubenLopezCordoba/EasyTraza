package cat.copernic.easytraza.domain.usecase

import cat.copernic.easytraza.domain.repository.LotRepository
import cat.copernic.easytraza.model.Lot

class GetLotsUseCase(private val repository: LotRepository) {
    suspend operator fun invoke(): Result<List<Lot>> = runCatching {
        repository.getLots()
    }
}
