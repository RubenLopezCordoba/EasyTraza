package cat.copernic.easytraza.ui.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.easytraza.data.repository.UsuariRepositoryImpl
import cat.copernic.easytraza.domain.usecase.GetUsuarisUseCase
import cat.copernic.easytraza.domain.usecase.LoginUseCase
import cat.copernic.easytraza.model.Usuari
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val usuarios: List<Usuari> = emptyList(),
    val isLoading: Boolean = false,
    val isLoggingIn: Boolean = false,
    val error: String? = null,
    val usuarioLogeado: Usuari? = null
)

class LoginViewModel(private val ip: String) : ViewModel() {

    private val usuariRepository = UsuariRepositoryImpl(ip)
    private val getUsuaris = GetUsuarisUseCase(usuariRepository)
    private val loginUseCase = LoginUseCase(usuariRepository)

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun carregarUsuaris() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            getUsuaris().fold(
                onSuccess = { _state.value = _state.value.copy(usuarios = it, isLoading = false) },
                onFailure = { _state.value = _state.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun ferLogin(usuariId: Long) {
        _state.value = _state.value.copy(isLoggingIn = true, error = null)
        viewModelScope.launch {
            loginUseCase(usuariId).fold(
                onSuccess = { _state.value = _state.value.copy(usuarioLogeado = it, isLoggingIn = false) },
                onFailure = { _state.value = _state.value.copy(error = it.message, isLoggingIn = false) }
            )
        }
    }

    fun tancarSessio() {
        cat.copernic.easytraza.network.RetrofitClient.clearSession()
        _state.value = LoginUiState()
    }
}
