package cat.copernic.easytraza.model

/** Classe per a la gestió de Provedor a l'aplicació EasyTraza. */
class Provedor(
    val id: Long = 0,
    val nombre: String = "",
    val telefono: String = "",
    val cif: String = "",
    val nif: String = "",
    val direccion: String = "",
    val observaciones: String = ""
)
