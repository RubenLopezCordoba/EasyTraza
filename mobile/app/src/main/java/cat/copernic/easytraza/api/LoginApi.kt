package cat.copernic.easytraza.api

import cat.copernic.easytraza.model.Usuari
import retrofit2.Call
import retrofit2.http.*

/** Classe per a la gestió de LoginApi a l'aplicació EasyTraza. */
data class LoginResponse(
    val success: Boolean,
    val usuari: Usuari?,
    val error: String?
)

interface LoginApi {

    @POST("api/mobile/login")
    fun login(@Body body: Map<String, Long>): Call<LoginResponse>

    @GET("api/mobile/usuarios")
    fun getUsuarios(): Call<List<Usuari>>
}
