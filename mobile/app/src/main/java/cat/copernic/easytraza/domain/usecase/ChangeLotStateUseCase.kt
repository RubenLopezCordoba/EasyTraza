package cat.copernic.easytraza.domain.usecase

import cat.copernic.easytraza.domain.repository.LotRepository

class ChangeLotStateUseCase(private val repository: LotRepository) {
    suspend operator fun invoke(nifProveidor: String, idLot: String, estat: String): Result<Unit> = runCatching {
        repository.updateLotEstat(nifProveidor, idLot, estat)
    }
}
