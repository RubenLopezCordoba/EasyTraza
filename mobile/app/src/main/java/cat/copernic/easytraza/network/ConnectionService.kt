package cat.copernic.easytraza.network

import android.content.Context
import cat.copernic.easytraza.api.SystemApi
import cat.copernic.easytraza.config.IpStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Classe per a la gestió de ConnectionService a l'aplicació EasyTraza.
 */
class ConnectionService(private val context: Context) {

    fun testConexion(onResult: (String) -> Unit) {
        val ip = IpStorage.getIp(context)
        val retrofit = RetrofitClient.getClient(ip)
        val api = retrofit.create(SystemApi::class.java)

        // Probar con diferentes endpoints
        api.ping().enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                if (response.isSuccessful) {
                    onResult("OK 🟢 CONECTADO - Servidor responde correctamente")
                } else {
                    when (response.code()) {
                        404 -> onResult("ERROR ⚠️ Endpoint /ping no encontrado\n" +
                                "Verifica que el servidor tenga la ruta /ping")
                        401 -> onResult("ERROR 🔴 No autorizado")
                        500 -> onResult("ERROR 🔴 Error interno del servidor")
                        else -> onResult("ERROR 🔴 SERVIDOR - Código: ${response.code()}")
                    }
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                onResult("FAIL 🔴 ${t.message}")
            }
        })
    }
}