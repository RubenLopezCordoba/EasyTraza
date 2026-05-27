package cat.copernic.easytraza.model

/** Classe per a la gestió de Proveedor a l'aplicació EasyTraza. */
class Proveedor(
    val id: Long = 0,
    val nombre: String = "",
    val telefono: String = "",
    val cif: String = "",
    val direccion: String = "",
    val observaciones: String = ""
)