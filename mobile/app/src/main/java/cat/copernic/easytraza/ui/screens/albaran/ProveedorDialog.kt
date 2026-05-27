package cat.copernic.easytraza.ui.screens.albaran

/** Diàleg per a la selecció i creació de proveïdors. */
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.R
import cat.copernic.easytraza.model.Provedor

@Composable
fun ProveedorDialog(
    proveedores: List<Provedor>,
    onDismiss: () -> Unit,
    onSelect: (Provedor) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.proveedor_dialog_titol)) },
        text = {
            LazyColumn {
                items(proveedores) { proveedor ->
                    Text(
                        text = "${proveedor.nombre} (${proveedor.cif})",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(proveedor) }
                            .padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.revisar_cancelar))
            }
        }
    )
}
