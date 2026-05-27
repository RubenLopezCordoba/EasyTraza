package cat.copernic.easytraza.api

import cat.copernic.easytraza.model.CatalogoItem
import retrofit2.Call
import retrofit2.http.GET

/**
 * Classe per a la gestió de CatalogoApi a l'aplicació EasyTraza.
 */
interface CatalogoApi {

    @GET("catalogo/api/listar")
    fun getAll(): Call<List<CatalogoItem>>

    @GET("catalogo/api/ingredientes")
    fun getIngredientes(): Call<List<CatalogoItem>>

    @GET("catalogo/api/productos")
    fun getProductos(): Call<List<CatalogoItem>>
}
