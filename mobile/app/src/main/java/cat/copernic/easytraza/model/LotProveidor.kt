package cat.copernic.easytraza.model

/** Classe per a la gestió de LotProveidor a l'aplicació EasyTraza. */
class LotProveidor(
    val id: Long = 0,
    val numLot: String = "",
    val catalogo: Catalogo? = null,          // Puede ser null si no existe en catálogo
    val quantitat: Int = 0,
    val estat: String = "EN_ESTOC",
    val albaraId: Long = 0,                  // ID del albarán padre
    val lotProveedor: String = "",            // Lote del proveedor (Ej: "M1952322")
    val dataCaducitat: String? = null,        // El backend envía String ISO
    val dataRecepcio: String? = null,         // El backend envía String ISO
    val dataObertura: String? = null,         // El backend envía String ISO
    val dataAcabament: String? = null,        // El backend envía String ISO
    val ubicacio: String? = null
)