package cat.copernic.easytraza.model

/** Classe per a la gestió de Lot a l'aplicació EasyTraza. */
class Lot(
    val nifProveidor: String? = "",
    val idLot: String? = "",
    val numLot: String? = "",
    val estat: String? = "EN_ESTOC",
    val catalogo: CatalogoRef? = null,
    val quantitat: Int = 0,
    val unitat: String? = "kg",
    val dataCaducitat: String? = null,
    val dataRecepcio: String? = null,
    val dataObertura: String? = null,
    val dataAcabament: String? = null
)

data class CatalogoRef(
    val id: Long = 0,
    val nombre: String = ""
)
