package cat.copernic.easytraza.model

/** Classe per a la gestió de AlbaraProveidor a l'aplicació EasyTraza. */
class AlbaraProveidor(
    val id: Long = 0,
    val numAlbara: String = "",
    val dataRecepcio: String = "",           // El backend envía String ISO: "2026-01-14T10:30:00"
    val proveedorNombre: String = "",         // Campo desnormalizado del backend
    val operarioNombre: String = "",          // Campo desnormalizado del backend
    val proveedorCif: String = "",            // Campo desnormalizado del backend
    val operarioEmail: String = "",           // Campo desnormalizado del backend
    val lots: List<LotProveidor> = emptyList(),
    val observacions: String = "",
    val estat: String = "PENDENT",
    val imagenUrl: String = "",
    val textoOcr: String = "",                // Nuevo campo OCR
    val jsonParsejat: String = ""             // Nuevo campo JSON parseado
)

// DTO para crear albarán manualmente (no se usa en OCR)
data class AlbaraWithLotsRequest(
    val numAlbara: String,
    val proveedorId: Long,
    val usuariId: Long,
    val observacions: String,
    val lots: List<LotProveidor>
)