package cat.copernic.easytraza.ui.viewmodels.albarans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.easytraza.data.repository.AlbaraRepositoryImpl
import cat.copernic.easytraza.domain.usecase.GetAlbaransUseCase
import cat.copernic.easytraza.model.AlbarraProveidor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AlbaransUiState(
    val albarans: List<AlbarraProveidor> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AlbaransViewModel(private val ip: String) : ViewModel() {

    private val repository = AlbaraRepositoryImpl(ip)
    private val getAlbarans = GetAlbaransUseCase(repository)

    private val _state = MutableStateFlow(AlbaransUiState())
    val state: StateFlow<AlbaransUiState> = _state

    fun carregarAlbarans() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            getAlbarans().fold(
                onSuccess = { _state.value = _state.value.copy(albarans = it, isLoading = false) },
                onFailure = { _state.value = _state.value.copy(error = it.message, isLoading = false) }
            )
        }
    }
}
