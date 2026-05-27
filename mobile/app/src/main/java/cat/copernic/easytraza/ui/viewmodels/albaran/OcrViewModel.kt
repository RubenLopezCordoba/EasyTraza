package cat.copernic.easytraza.ui.viewmodels.albaran

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.easytraza.api.*
import cat.copernic.easytraza.config.IpStorage
import cat.copernic.easytraza.model.Provedor
import cat.copernic.easytraza.model.Usuari
import cat.copernic.easytraza.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

/**
 * Classe per a la gestió de OcrViewModel a l'aplicació EasyTraza.
 */
class OcrViewModel(application: Application) : AndroidViewModel(application) {

    private fun getIp(): String = IpStorage.getIp(getApplication())
    private fun getApi(): OcrApi = RetrofitClient.getClient(getIp()).create(OcrApi::class.java)

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class AnalisisReady(val datos: AlbaranParsedDTO) : UiState()
        data class Success(val response: OcrResponse) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private val _proveedores = MutableStateFlow<List<Provedor>>(emptyList())
    val proveedores: StateFlow<List<Provedor>> = _proveedores

    private val _usuarios = MutableStateFlow<List<Usuari>>(emptyList())
    val usuarios: StateFlow<List<Usuari>> = _usuarios

    private val gson = com.google.gson.Gson()
    private var activeJob: kotlinx.coroutines.Job? = null

    init {
        cargarProveedores()
        cargarUsuarios()
    }

    private fun cargarProveedores() {
        viewModelScope.launch {
            try {
                val call = RetrofitClient.getClient(getIp()).create(ProveedorApi::class.java).getProveedores()
                val response = withContext(Dispatchers.IO) { call.execute() }
                if (response.isSuccessful) _proveedores.value = response.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    private fun cargarUsuarios() {
        viewModelScope.launch {
            try {
                val call = RetrofitClient.getClient(getIp()).create(UsuarioApi::class.java).getUsuarios()
                val response = withContext(Dispatchers.IO) { call.execute() }
                if (response.isSuccessful) _usuarios.value = response.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    fun analizarAlbaran(inputStream: InputStream) {
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val imageBytes = withContext(Dispatchers.IO) { inputStream.readBytes() }
                val requestBody = imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("imagen", "albaran.jpg", requestBody)
                val response = try { getApi().analizarAlbaran(imagePart) } catch (e: Exception) {
                    _uiState.value = UiState.Error("Error de connexió: ${e.message}"); return@launch
                }
                val rawBody = withContext(Dispatchers.IO) { response.body()?.string() }
                if (response.isSuccessful && rawBody != null) {
                    try {
                        val analisis = withContext(Dispatchers.Default) { gson.fromJson(rawBody, AnalisisResponse::class.java) }
                        if (analisis.success && analisis.datos != null) {
                            _uiState.value = UiState.AnalisisReady(analisis.datos)
                        } else {
                            _uiState.value = UiState.Error(analisis.mensaje)
                        }
                    } catch (e: Exception) {
                        _uiState.value = UiState.Error("Resposta invàlida: $rawBody")
                    }
                } else {
                    val errorBody = withContext(Dispatchers.IO) { response.errorBody()?.string() }
                    val msg = if (errorBody != null) {
                        try {
                            val errJson = withContext(Dispatchers.Default) { gson.fromJson(errorBody, Map::class.java) }
                            (errJson["mensaje"] as? String) ?: (errJson["error"] as? String) ?: "Error de servidor (${response.code()})"
                        } catch (_: Exception) {
                            errorBody
                        }
                    } else "Error de servidor (${response.code()})"
                    _uiState.value = UiState.Error(msg)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error: ${e.message}")
            }
        }
    }

    fun confirmarAlbaran(datos: AlbaranParsedDTO, usuarioId: Long) {
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = try { getApi().confirmarAlbaran(datos, usuarioId) } catch (e: Exception) {
                    _uiState.value = UiState.Error("Error de connexió: ${e.message}"); return@launch
                }
                val rawBody = withContext(Dispatchers.IO) { response.body()?.string() }
                if (response.isSuccessful && rawBody != null) {
                    try {
                        val ocrResponse = withContext(Dispatchers.Default) { gson.fromJson(rawBody, OcrResponse::class.java) }
                        if (ocrResponse.success) {
                            _uiState.value = UiState.Success(ocrResponse)
                        } else {
                            _uiState.value = UiState.Error(ocrResponse.mensaje)
                        }
                    } catch (e: Exception) {
                        _uiState.value = UiState.Error("Resposta invàlida: $rawBody")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val msg = if (errorBody != null) {
                        try {
                            val errJson = gson.fromJson(errorBody, Map::class.java)
                            (errJson["mensaje"] as? String) ?: (errJson["error"] as? String) ?: "Error de servidor (${response.code()})"
                        } catch (_: Exception) {
                            errorBody
                        }
                    } else "Error de servidor (${response.code()})"
                    _uiState.value = UiState.Error(msg)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error: ${e.message}")
            }
        }
    }

    fun resetState() {
        activeJob?.cancel()
        _uiState.value = UiState.Idle
    }
}
