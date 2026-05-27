package cat.copernic.easytraza.model

/** Classe per a la gestió de AlbarraProveidor a l'aplicació EasyTraza. */
class AlbarraProveidor(
    val nifProveidor: String? = "",
    val numAlbara: String? = "",
    val dataRecepcio: String? = "",
    val proveedor: Provedor? = null,
    val proveedorNombre: String? = "",
    val usuario: Usuari? = null,
    val operarioNombre: String? = "",
    val lots: List<Lot>? = emptyList(),
    val observacions: String? = "",
    val imagenUrl: String? = ""
)
