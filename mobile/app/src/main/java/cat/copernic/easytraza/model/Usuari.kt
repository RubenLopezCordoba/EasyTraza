package cat.copernic.easytraza.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

/** Model de dades d'usuari per al client mòbil. */
data class Usuari(
    val id: Long = 0,
    val nombre: String = "",
    val email: String = "",
    val rol: String = "",
    val fotoUrl: String? = null,
    val nif: String? = null
)
