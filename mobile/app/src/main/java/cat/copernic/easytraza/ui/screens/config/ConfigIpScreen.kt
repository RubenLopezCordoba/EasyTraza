package cat.copernic.easytraza.ui.screens.config


/** Pantalla de configuració de l'adreça IP del servidor. */
import cat.copernic.easytraza.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.config.IpStorage
import cat.copernic.easytraza.network.ConnectionService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigIpScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val service = remember { ConnectionService(context) }
    val scope = rememberCoroutineScope()

    var ip by remember { mutableStateOf(IpStorage.getIp(context)) }
    var result by remember { mutableStateOf("") }
    var isTesting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.config_ip_titol)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("← " + stringResource(cat.copernic.easytraza.R.string.sortir), style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Tarjeta informativa
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "ℹ️ Información",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Introduce la dirección IP del servidor al que quieres conectar la aplicación.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Formato: 192.168.1.85",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Campo de texto para la IP
            OutlinedTextField(
                value = ip,
                onValueChange = { ip = it.trim() },
                label = { Text(stringResource(R.string.config_ip_label)) },
                placeholder = { Text(stringResource(R.string.config_ip_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = result.contains("ERROR") || result.contains("FAIL"),
                supportingText = {
                    if (result.contains("ERROR") || result.contains("FAIL")) {
                        Text(
                            text = "La IP no es válida o no se puede conectar",
                            color = Color.Red
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // IP actual guardada
            Text(
                text = "IP actual guardada: ${IpStorage.getIp(context)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Guardar IP
            Button(
                onClick = {
                    if (ip.isNotEmpty() && esIpValida(ip)) {
                        IpStorage.saveIp(context, ip)
                        result = "✅ IP guardada correctamente: $ip"
                    } else {
                        result = "❌ IP inválida. Formato incorrecto"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    androidx.compose.material.icons.Icons.Default.Check,
                    contentDescription = stringResource(R.string.config_ip_guardar),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.config_ip_guardar))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón Probar conexión
            Button(
                onClick = {
                    if (ip.isNotEmpty()) {
                        isTesting = true
                        result = "🔄 Probando conexión con $ip..."

                        // Guardar temporalmente la IP para la prueba
                        val ipOriginal = IpStorage.getIp(context)
                        IpStorage.saveIp(context, ip)

                        service.testConexion { res ->
                            result = res
                            isTesting = false

                            // Restaurar IP original si no se guardó
                            if (ip != IpStorage.getIp(context)) {
                                IpStorage.saveIp(context, ipOriginal)
                            }
                        }
                    } else {
                        result = "❌ Introduce una IP primero"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTesting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.config_ip_guardar),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isTesting) "Probando..." else "Probar conexión")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mostrar resultado
            if (result.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            result.contains("✅") || result.contains("OK") -> Color(0xFFE8F5E9)
                            result.contains("❌") || result.contains("ERROR") || result.contains("FAIL") -> Color(0xFFFFEBEE)
                            result.contains("🔄") -> Color(0xFFE3F2FD)
                            else -> Color(0xFFF5F5F5)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = result,
                        modifier = Modifier.padding(16.dp),
                        color = when {
                            result.contains("✅") || result.contains("OK") -> Color(0xFF2E7D32)
                            result.contains("❌") || result.contains("ERROR") || result.contains("FAIL") -> Color(0xFFC62828)
                            result.contains("🔄") -> Color(0xFF1976D2)
                            else -> Color.Gray
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de prueba rápida con IP predeterminada
            OutlinedButton(
                onClick = {
                    ip = "192.168.1.85"
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.config_ip_restaurar))
            }
        }
    }
}

// Función de validación de IP
private fun esIpValida(ip: String): Boolean {
    val pattern = Regex(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    )
    return pattern.matches(ip)
}