package cat.copernic.easytraza.api

import retrofit2.Call
import retrofit2.http.GET

/**
 * Classe per a la gestió de SystemApi a l'aplicació EasyTraza.
 */
interface SystemApi {
    @GET("system/ping")
    fun ping(): Call<Map<String, Any>>
}