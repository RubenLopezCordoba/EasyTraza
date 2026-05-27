package cat.copernic.easytraza.api

import cat.copernic.easytraza.model.AlbaraProveidor
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Classe per a la gestió de OcrApi a l'aplicació EasyTraza.
 */
interface OcrApi {

    @Multipart
    @POST("api/ocr/analizar")
    suspend fun analizarAlbaran(
        @Part imagen: MultipartBody.Part
    ): Response<ResponseBody>

    @POST("api/ocr/confirmar")
    suspend fun confirmarAlbaran(
        @Body datos: AlbaranParsedDTO,
        @Query("usuarioId") usuarioId: Long
    ): Response<ResponseBody>

    @GET("api/ocr/test")
    suspend fun testOcr(): Response<TestResponse>
}

// ========== CLASES DE RESPUESTA ==========

data class AnalisisResponse(
    val success: Boolean,
    val mensaje: String,
    val datos: AlbaranParsedDTO?
)

data class AlbaranParsedDTO(
    val numAlbara: String? = "",
    val proveedor: ProveedorParsed? = null,
    val fecha: String? = "",
    val lots: List<LoteParsed> = emptyList(),
    val imagenTemporal: String? = "",
    val observacions: String? = ""
)

data class ProveedorParsed(
    val cif: String? = "",
    val nombre: String? = "",
    val direccion: String? = ""
)

data class LoteParsed(
    val codigoArticulo: String? = "",
    val descripcion: String? = "",
    val cantidad: Double = 0.0,
    val unidad: String? = "",
    val lote: String? = "",
    val fechaConsumo: String? = ""
)

data class OcrResponse(
    val success: Boolean,
    val mensaje: String,
    val albaran: AlbaraProveidor?
)

data class TestResponse(
    val estado: String,
    val mensaje: String
)