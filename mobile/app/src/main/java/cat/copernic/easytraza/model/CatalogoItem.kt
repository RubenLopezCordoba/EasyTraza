package cat.copernic.easytraza.model

/** Classe per a la gestió de CatalogoItem a l'aplicació EasyTraza. */
class CatalogoItem(
    val id: Long = 0,
    val nombre: String = "",
    val descripcion: String = "",
    val tipo: String = "INGREDIENTE"
)
