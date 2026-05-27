package cat.copernic.easytraza.ui.screens.login

import cat.copernic.easytraza.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.easytraza.model.Usuari

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    usuarios: List<Usuari>,
    isLoading: Boolean,
    errorMessage: String?,
    ipActual: String,
    onUsuarioSelected: (Long) -> Unit,
    onConfigIp: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Image(painterResource(R.drawable.logo), contentDescription = "EasyTraza", modifier = Modifier.width(220.dp).height(110.dp))
        Spacer(Modifier.height(8.dp))

        Text(stringResource(R.string.app_name), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.app_subtitle), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(32.dp))

        Text(stringResource(R.string.login_seleccionar), fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.login_servidor, ipActual), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.login_cargando))
                }
            }
        } else if (errorMessage != null) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(errorMessage, modifier = Modifier.padding(16.dp))
            }
        } else if (usuarios.isEmpty()) {
            Text(stringResource(R.string.login_no_usuarios), textAlign = TextAlign.Center)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(usuarios) { u ->
                    Card(
                        Modifier.fillMaxWidth().clickable { onUsuarioSelected(u.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(52.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(u.nombre.take(2).uppercase(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(u.nombre, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                                Text(u.rol, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
        TextButton(onClick = onConfigIp) { Text(stringResource(R.string.cambiar_servidor), fontSize = 14.sp) }
        Spacer(Modifier.height(16.dp))
    }
}
