package cat.copernic.easytraza.api

import cat.copernic.easytraza.model.AlbarraProveidor
import retrofit2.Call
import retrofit2.http.*

/**
 * Classe per a la gestió de AlbaraApi a l'aplicació EasyTraza.
 */
interface AlbaraApi {

    @GET("api/albarans")
    fun getAlbarans(): Call<List<AlbarraProveidor>>

    @GET("api/albarans/{nifProveidor}/{idAlbarra}")
    fun getAlbaraById(@Path("nifProveidor") nifProveidor: String, @Path("idAlbarra") idAlbarra: String): Call<AlbarraProveidor>
}
