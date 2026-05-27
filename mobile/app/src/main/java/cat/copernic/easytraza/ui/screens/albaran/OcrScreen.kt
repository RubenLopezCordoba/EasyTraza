package cat.copernic.easytraza.ui.screens.albaran

/** Pantalla de captura i anàlisi OCR d'albarans de proveïdor. */
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import cat.copernic.easytraza.R
import cat.copernic.easytraza.api.AlbaranParsedDTO
import cat.copernic.easytraza.ui.viewmodels.albaran.OcrViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(
    usuarioId: Long = 1L,
    onBack: () -> Unit = {},
    viewModel: OcrViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val proveedores by viewModel.proveedores.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var savedDatos by remember { mutableStateOf<AlbaranParsedDTO?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var isConfirming by remember { mutableStateOf(false) }

    // Pre-resolve string resources for use in non-composable lambdas
    val strErrorCamaraNoDisponible = stringResource(R.string.ocr_error_camara_no_disponible)
    val strErrorAbrirCamara = stringResource(R.string.ocr_error_abrir_camara)
    val strErrorPermisoCamara = stringResource(R.string.ocr_error_permiso_camara)
    val strErrorSinCamara = stringResource(R.string.ocr_error_sin_camara)
    val strErrorAbrirImagen = stringResource(R.string.ocr_error_abrir_imagen)
    val strErrorPermisoImagen = stringResource(R.string.ocr_error_permiso_imagen)
    val strErrorAbrirImagenMsg = stringResource(R.string.ocr_error_abrir_imagen_msg)

    fun goBack() {
        savedDatos = null
        isConfirming = false
        confirmError = null
        viewModel.resetState()
        onBack()
    }

    // Lanzador para hacer foto con la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            selectedImageUri = cameraImageUri ?: result.data?.data
        }
    }

    fun launchCamera() {
        try {
            val photoFile = File(context.cacheDir, "ocr_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraImageUri = uri
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                cameraLauncher.launch(intent)
            } else {
                errorMsg = strErrorCamaraNoDisponible
            }
        } catch (e: Exception) {
            errorMsg = strErrorAbrirCamara.replace("%s", e.message ?: "")
        }
    }

    // Lanzador para permisos de cámara
    val permRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            errorMsg = strErrorPermisoCamara
        }
    }

    // Lanzador para seleccionar imagen de galería
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            cameraImageUri = null
        }
    }
    // Guardar datos analizados para mantener la pantalla de revisión si falla confirmación
    val currentState = uiState

    // Al recibir datos analizados, guardarlos y mostrar revisión
    if (currentState is OcrViewModel.UiState.AnalisisReady && !isConfirming) {
        savedDatos = currentState.datos
        confirmError = null
    }

    // Si hay datos guardados, mostrar siempre la pantalla de revisión
    // (incluso durante loading y error, para no perder las ediciones del usuario)
    if (savedDatos != null && currentState !is OcrViewModel.UiState.Success) {
        if (currentState is OcrViewModel.UiState.Loading) isConfirming = true
        if (currentState is OcrViewModel.UiState.Error) {
            confirmError = currentState.message
            isConfirming = false
        }
        OcrReviewScreen(
            datosAnalizados = savedDatos!!,
            proveedores = proveedores,
            usuarios = usuarios,
            usuarioIdInicial = usuarioId,
            errorMessage = confirmError,
            isConfirming = isConfirming,
            onErrorClear = { confirmError = null },
            onConfirmar = { datos, usuId ->
                confirmError = null
                isConfirming = true
                viewModel.confirmarAlbaran(datos, usuId)
            },
            onCancelar = { savedDatos = null; viewModel.resetState() },
            onBack = { savedDatos = null; viewModel.resetState(); onBack() },
        )
        return
    }

    if (currentState is OcrViewModel.UiState.Success) {
        savedDatos = null
        confirmError = null
        isConfirming = false
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            viewModel.resetState()
            selectedImageUri = null
            isProcessing = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.ocr_titol)) },
            navigationIcon = {
                TextButton(onClick = { goBack() }) {
                    Text("← ${stringResource(R.string.ocr_volver)}", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.ocr_subtitol), fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.ocr_galeria))
                }

                Button(
                    onClick = {
                        errorMsg = null
                        val hasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
                        if (!hasCamera) {
                            errorMsg = strErrorSinCamara
                            return@Button
                        }
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED
                        ) {
                            launchCamera()
                        } else {
                            // Guardar URI temporal y pedir permiso
                            val photoFile = File(context.cacheDir, "ocr_${System.currentTimeMillis()}.jpg")
                            cameraImageUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            permRequestLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.ocr_camara))
                }
            }

            errorMsg?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text("⚠️ $it", modifier = Modifier.padding(12.dp))
                }
            }

            selectedImageUri?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text("✅ ${it.lastPathSegment}", modifier = Modifier.padding(16.dp))
                }
            }

            Button(
                onClick = {
                    selectedImageUri?.let { uri ->
                        try {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            if (inputStream != null) {
                                isProcessing = true
                                viewModel.analizarAlbaran(inputStream)
                            } else {
                                errorMsg = strErrorAbrirImagen
                            }
                        } catch (e: SecurityException) {
                            errorMsg = strErrorPermisoImagen
                        } catch (e: Exception) {
                            errorMsg = strErrorAbrirImagenMsg.replace("%s", e.message ?: "")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedImageUri != null && !isProcessing
            ) {
                Text(if (isProcessing) stringResource(R.string.ocr_analizando) else stringResource(R.string.ocr_analizar))
            }

            when (uiState) {
                is OcrViewModel.UiState.Loading -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(stringResource(R.string.ocr_procesando))
                        }
                    }
                }

                is OcrViewModel.UiState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.ocr_guardado), fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.ocr_escanear_otro))
                        }
                    }
                }

                is OcrViewModel.UiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text("❌ ${(uiState as OcrViewModel.UiState.Error).message}", modifier = Modifier.padding(16.dp))
                    }
                    Button(onClick = {
                        viewModel.resetState()
                        isProcessing = false
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.ocr_intentar))
                    }
                }

                else -> {}
            }
        }
    }
}
