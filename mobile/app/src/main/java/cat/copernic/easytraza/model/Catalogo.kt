package cat.copernic.easytraza.model

/** Classe per a la gestió de Catalogo a l'aplicació EasyTraza. */
class Catalogo(
    val id: Long = 0,
    val tipo: String = "",        // "INGREDIENTE" o "PRODUCTO"
    val nombre: String = "",
    val descripcion: String = ""
)