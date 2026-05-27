package cat.copernic.easytraza.ui.viewmodels.lots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.easytraza.data.repository.LotRepositoryImpl
import cat.copernic.easytraza.domain.usecase.ChangeLotStateUseCase
import cat.copernic.easytraza.domain.usecase.GetLotsUseCase
import cat.copernic.easytraza.model.Lot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LotsUiState(
    val lots: List<Lot> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class LotsViewModel(private val ip: String) : ViewModel() {

    private val repository = LotRepositoryImpl(ip)
    private val getLots = GetLotsUseCase(repository)
    private val changeState = ChangeLotStateUseCase(repository)

    private val _state = MutableStateFlow(LotsUiState())
    val state: StateFlow<LotsUiState> = _state

    fun carregarLots() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            getLots().fold(
                onSuccess = { _state.value = _state.value.copy(lots = it, isLoading = false) },
                onFailure = { _state.value = _state.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun canviarEstat(nifProveidor: String, idLot: String, estat: String) {
        viewModelScope.launch {
            changeState(nifProveidor, idLot, estat).fold(
                onSuccess = { carregarLots() },
                onFailure = { _state.value = _state.value.copy(error = it.message) }
            )
        }
    }
}
