package cat.copernic.easytraza.ui.screens.albaran

/** Pantalla de revisió de dades extretes per OCR abans de guardar. */
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.R
import cat.copernic.easytraza.api.AlbaranParsedDTO
import cat.copernic.easytraza.api.LoteParsed
import cat.copernic.easytraza.api.ProveedorParsed
import cat.copernic.easytraza.model.Provedor
import cat.copernic.easytraza.model.Usuari
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrReviewScreen(
    datosAnalizados: AlbaranParsedDTO,
    proveedores: List<Provedor>,
    usuarios: List<Usuari>,
    usuarioIdInicial: Long,
    errorMessage: String?,
    isConfirming: Boolean = false,
    onErrorClear: () -> Unit,
    onConfirmar: (AlbaranParsedDTO, Long) -> Unit,
    onCancelar: () -> Unit,
    onBack: () -> Unit
) {
    var datos by remember { mutableStateOf(
        if (datosAnalizados.fecha.isNullOrBlank())
            datosAnalizados.copy(fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        else datosAnalizados
   ) }
    var selectedProveedor by remember { mutableStateOf<Provedor?>(null) }
    var showProveedorDialog by remember { mutableStateOf(false) }
    var selectedUsuarioId by remember { mutableStateOf(usuarioIdInicial) }
    var proveedorText by remember { mutableStateOf(
        datosAnalizados.proveedor?.let { "${it.nombre.orEmpty()} (${it.cif.orEmpty()})" } ?: ""
    ) }

    val sessionUser = usuarios.find { it.id == usuarioIdInicial }
    val unidades = listOf("kg", "g", "L", "TONELADAS", "SACOS", "UDS")
    var cantidadTexts by remember { mutableStateOf(mapOf<Int, String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.revisar_titol)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("←", color = MaterialTheme.colorScheme.onPrimary, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                    }
                },
                actions = {
                    TextButton(onClick = onCancelar) {
                        Text(stringResource(R.string.revisar_cancelar))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.revisar_num_albara), style = MaterialTheme.typography.labelSmall)
            OutlinedTextField(
                value = datos.numAlbara.orEmpty(),
                onValueChange = { datos = datos.copy(numAlbara = it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Text(stringResource(R.string.revisar_fecha), style = MaterialTheme.typography.labelSmall)
            DateField(
                value = datos.fecha.orEmpty(),
                onValueChange = { datos = datos.copy(fecha = it) }
            )

            Spacer(Modifier.height(8.dp))

            Text(stringResource(R.string.revisar_proveedor), style = MaterialTheme.typography.labelSmall)
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = proveedorText,
                    onValueChange = { proveedorText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.revisar_proveedor_placeholder)) }
                )
                TextButton(onClick = { showProveedorDialog = true }) {
                    Text(stringResource(R.string.revisar_seleccionar))
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(stringResource(R.string.revisar_usuari), style = MaterialTheme.typography.labelSmall)
            Text(
                text = sessionUser?.nombre ?: stringResource(R.string.revisar_usuari_default, usuarioIdInicial),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.revisar_observaciones), style = MaterialTheme.typography.labelSmall)
            OutlinedTextField(
                value = datos.observacions.orEmpty(),
                onValueChange = { datos = datos.copy(observacions = it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.revisar_productes), style = MaterialTheme.typography.titleMedium)

            datos.lots?.let { lots ->
                lots.forEachIndexed { index, lote ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.revisar_producte, index + 1), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                TextButton(onClick = {
                                    val newLots = lots.toMutableList()
                                    newLots.removeAt(index)
                                    datos = datos.copy(lots = newLots)
                                }) { Text(stringResource(R.string.revisar_eliminar), color = MaterialTheme.colorScheme.error) }
                            }

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = lote.descripcion.orEmpty(),
                                onValueChange = { newDesc ->
                                    val newLots = lots.toMutableList()
                                    newLots[index] = lote.copy(descripcion = newDesc)
                                    datos = datos.copy(lots = newLots)
                                },
                                label = { Text(stringResource(R.string.revisar_descripcio)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val cantStr = cantidadTexts[index] ?: formatCantidad(lote.cantidad)
                                OutlinedTextField(
                                    value = cantStr,
                                    onValueChange = { newCant ->
                                        cantidadTexts = cantidadTexts + (index to newCant)
                                        val parsed = newCant.toDoubleOrNull()
                                        if (parsed != null) {
                                            val newLots = lots.toMutableList()
                                            newLots[index] = lote.copy(cantidad = parsed)
                                            datos = datos.copy(lots = newLots)
                                        }
                                    },
                                    label = { Text(stringResource(R.string.revisar_quantitat)) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                var expandedUnidad by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expandedUnidad,
                                    onExpandedChange = { expandedUnidad = it }
                                ) {
                                    OutlinedTextField(
                                        value = lote.unidad.orEmpty(),
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnidad) },
                                        modifier = Modifier.width(100.dp).menuAnchor(),
                                        label = { Text(stringResource(R.string.revisar_unitat)) },
                                        singleLine = true
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedUnidad,
                                        onDismissRequest = { expandedUnidad = false }
                                    ) {
                                        unidades.forEach { unidad ->
                                            DropdownMenuItem(
                                                text = { Text(unidad) },
                                                onClick = {
                                                    val newLots = lots.toMutableList()
                                                    newLots[index] = lote.copy(unidad = unidad)
                                                    datos = datos.copy(lots = newLots)
                                                    expandedUnidad = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = lote.lote.orEmpty(),
                                onValueChange = { newLote ->
                                    val newLots = lots.toMutableList()
                                    newLots[index] = lote.copy(lote = newLote)
                                    datos = datos.copy(lots = newLots)
                                },
                                label = { Text(stringResource(R.string.revisar_lot)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(Modifier.height(8.dp))

                            DateField(
                                value = lote.fechaConsumo.orEmpty(),
                                onValueChange = { newFecha ->
                                    val newLots = lots.toMutableList()
                                    newLots[index] = lote.copy(fechaConsumo = newFecha)
                                    datos = datos.copy(lots = newLots)
                                },
                                label = stringResource(R.string.revisar_caducitat)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    val newLots = (datos.lots ?: emptyList()).toMutableList()
                    newLots.add(LoteParsed())
                    datos = datos.copy(lots = newLots)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.revisar_afegir))
            }

            Spacer(Modifier.height(16.dp))

            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(errorMessage, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
                Spacer(Modifier.height(8.dp))
            }

            if (isConfirming) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    val provText = proveedorText.trim()
                    val proveedor = if (provText.isNotEmpty()) {
                        val match = Regex("(.+?)\\s*\\(([^)]+)\\)").find(provText)
                        if (match != null) {
                            ProveedorParsed(nombre = match.groupValues[1].trim(), cif = match.groupValues[2].trim())
                        } else {
                            ProveedorParsed(nombre = provText, cif = provText)
                        }
                    } else null
                    val datosFinal = if (proveedor != null) datos.copy(proveedor = proveedor) else datos
                    onConfirmar(datosFinal, selectedUsuarioId)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isConfirming
            ) {
                Text(if (isConfirming) stringResource(R.string.revisar_guardando) else stringResource(R.string.revisar_confirmar))
            }
        }

        if (showProveedorDialog) {
            ProveedorDialog(
                proveedores = proveedores,
                onDismiss = { showProveedorDialog = false },
                onSelect = { prov ->
                    selectedProveedor = prov
                    proveedorText = "${prov.nombre} (${prov.cif})"
                    datos = datos.copy(proveedor = ProveedorParsed(
                        cif = prov.cif,
                        nombre = prov.nombre,
                        direccion = prov.direccion
                    ))
                    showProveedorDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(value: String, onValueChange: (String) -> Unit, label: String = stringResource(R.string.revisar_fecha)) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = parseDateToMillis(value)
    )

    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("dd/MM/yyyy") }
        )
        Box(
            modifier = Modifier.matchParentSize().clickable { showDatePicker = true }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        onValueChange(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.revisar_cancelar))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun parseDateToMillis(dateStr: String): Long? {
    if (dateStr.isBlank()) return null
    return try {
        val date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    } catch (_: Exception) { null }
}

private fun formatCantidad(value: Double): String {
    return if (value == value.toLong().toDouble()) value.toLong().toString() else String.format("%.2f", value)
}
