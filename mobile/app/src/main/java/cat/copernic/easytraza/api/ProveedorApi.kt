package cat.copernic.easytraza.api

import cat.copernic.easytraza.model.Provedor
import retrofit2.Call
import retrofit2.http.GET

/**
 * Classe per a la gestió de ProveedorApi a l'aplicació EasyTraza.
 */
interface ProveedorApi {

    @GET("proveedores/api/listar")
    fun getProveedores(): Call<List<Provedor>>
}
