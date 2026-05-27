package cat.copernic.easytraza.api

import cat.copernic.easytraza.model.Lot
import retrofit2.Call
import retrofit2.http.*

/**
 * Classe per a la gestió de LotApi a l'aplicació EasyTraza.
 */
interface LotApi {

    @GET("api/lots")
    fun getLots(): Call<List<Lot>>

    @GET("api/lots/oberts")
    fun getLotsOberts(): Call<List<Lot>>

    @PUT("api/lots/{nifProveidor}/iniciar")
    fun iniciarLot(@Path("nifProveidor") nifProveidor: String, @Query("idLot") idLot: String): Call<Map<String, Any>>

    @PUT("api/lots/{nifProveidor}/finalitzar")
    fun finalitzarLot(@Path("nifProveidor") nifProveidor: String, @Query("idLot") idLot: String): Call<Map<String, Any>>
}
