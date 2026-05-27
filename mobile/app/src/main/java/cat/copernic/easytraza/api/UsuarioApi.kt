package cat.copernic.easytraza.api


import cat.copernic.easytraza.model.Usuari
import retrofit2.Call
import retrofit2.http.*

/**
 * Classe per a la gestió de UsuarioApi a l'aplicació EasyTraza.
 */
interface UsuarioApi {

    @GET("login/api/usuarios")
    fun getUsuarios(): Call<List<Usuari>>
}
