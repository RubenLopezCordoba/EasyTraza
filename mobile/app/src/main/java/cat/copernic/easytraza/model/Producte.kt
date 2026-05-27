package cat.copernic.easytraza.model

/** Classe per a la gestió de Producte a l'aplicació EasyTraza. */
class Producte(
    val id: Long = 0,
    val nombre: String = "",
    val descripcion: String = "",
    val tipo: String = "PRODUCTO"
)

data class MateriaPrimera(
    val id: Long = 0,
    val nombre: String = "",
    val descripcion: String = "",
    val tipo: String = "INGREDIENTE"
)
