package cat.copernic.easytraza.ui.screens.albarans

import cat.copernic.easytraza.R
import cat.copernic.easytraza.model.AlbarraProveidor
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbaransScreen(
    ip: String,
    albarans: List<AlbarraProveidor>,
    isLoading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onRefresh: () -> Unit = {}
) {
    var selectedAlbara by remember { mutableStateOf<AlbarraProveidor?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredAlbarans = albarans.filter { a ->
        searchQuery.isBlank() ||
        a.numAlbara?.contains(searchQuery, ignoreCase = true) == true ||
        a.proveedor?.nombre?.contains(searchQuery, ignoreCase = true) == true ||
        a.proveedorNombre?.contains(searchQuery, ignoreCase = true) == true ||
        a.nifProveidor?.contains(searchQuery, ignoreCase = true) == true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.albara_titol)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary),
                navigationIcon = { TextButton(onClick = onBack) { Text("← " + stringResource(cat.copernic.easytraza.R.string.tancar), color = MaterialTheme.colorScheme.onPrimary) } },
                actions = { TextButton(onClick = onRefresh) { Text(stringResource(R.string.lots_refrescar), color = MaterialTheme.colorScheme.onPrimary) } }
            )
        }
    ) { pv ->
        Column(Modifier.fillMaxSize().padding(pv).padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.albara_cercar)) },
                singleLine = true,
                leadingIcon = { Text("🔍") }
            )

            Spacer(Modifier.height(8.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
            } else if (filteredAlbarans.isEmpty()) {
                Text(if (searchQuery.isBlank()) stringResource(R.string.albara_buit) else stringResource(R.string.albara_no_coincidencies))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredAlbarans) { a ->
                        Card(Modifier.fillMaxWidth().clickable { selectedAlbara = a }) {
                            Column(Modifier.padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(stringResource(R.string.albara_card_titol, a.numAlbara.orEmpty()), fontWeight = FontWeight.Bold)
                                    Text("${a.lots?.size ?: 0} ${stringResource(R.string.albara_card_lots)}", style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(stringResource(R.string.albara_card_proveidor, a.proveedor?.nombre.orEmpty().ifEmpty { a.proveedorNombre.orEmpty() }.ifEmpty { "-" }), style = MaterialTheme.typography.bodyMedium)
                                Text(stringResource(R.string.albara_card_data, a.dataRecepcio?.split("T")?.get(0) ?: "-"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (!a.observacions.orEmpty().isBlank()) {
                                    Text(stringResource(R.string.albara_card_obs, a.observacions.orEmpty()), style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedAlbara != null) {
        val a = selectedAlbara!!
        AlertDialog(
            onDismissRequest = { selectedAlbara = null },
            title = { Text(stringResource(R.string.albara_detall, a.numAlbara.orEmpty())) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.albara_card_proveidor, a.proveedor?.nombre.orEmpty().ifEmpty { a.proveedorNombre.orEmpty() }.ifEmpty { "-" }))
                    Text(stringResource(R.string.albara_card_data, a.dataRecepcio?.split("T")?.get(0) ?: "-"))
                    if (!a.observacions.orEmpty().isBlank()) Text(stringResource(R.string.albara_card_obs, a.observacions.orEmpty()))
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.albara_lots), fontWeight = FontWeight.Bold)
                    a.lots?.forEach { lot ->
                        Text("• ${lot.catalogo?.nombre.orEmpty().ifEmpty { "-" }} (${lot.estat.orEmpty()})", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selectedAlbara = null }) { Text(stringResource(R.string.tancar)) } }
        )
    }
}
