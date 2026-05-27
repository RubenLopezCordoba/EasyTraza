package cat.copernic.easytraza.ui.screens.lots

import cat.copernic.easytraza.R
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
import cat.copernic.easytraza.model.Lot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotsScreen(
    lots: List<Lot>,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onStartLot: (String, String) -> Unit,
    onFinishLot: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("TOTS") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredLots = lots
        .filter { lot ->
            when (selectedFilter) {
                "OBERT" -> lot.estat == "OBERT"
                "ACABAT" -> lot.estat == "ACABAT"
                "EN_ESTOC" -> lot.estat == "EN_ESTOC"
                else -> true
            }
        }
        .filter { lot ->
            searchQuery.isBlank() ||
            lot.numLot?.contains(searchQuery, ignoreCase = true) == true ||
            lot.idLot?.contains(searchQuery, ignoreCase = true) == true ||
            lot.catalogo?.nombre?.contains(searchQuery, ignoreCase = true) == true ||
            lot.nifProveidor?.contains(searchQuery, ignoreCase = true) == true
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lots_titol)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← " + stringResource(cat.copernic.easytraza.R.string.tancar), color = MaterialTheme.colorScheme.onPrimary) }
                }
            )
        }
    ) { pv ->
        Column(Modifier.fillMaxSize().padding(pv).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Lots", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onRefresh) { Text(stringResource(R.string.lots_refrescar)) }
            }
            Spacer(Modifier.height(8.dp))

            // Filter chips
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFilter == "TOTS",
                    onClick = { selectedFilter = "TOTS" },
                    label = { Text("Tots") }
                )
                FilterChip(
                    selected = selectedFilter == "OBERT",
                    onClick = { selectedFilter = "OBERT" },
                    label = { Text("Oberts") }
                )
                FilterChip(
                    selected = selectedFilter == "EN_ESTOC",
                    onClick = { selectedFilter = "EN_ESTOC" },
                    label = { Text("Stock") }
                )
                FilterChip(
                    selected = selectedFilter == "ACABAT",
                    onClick = { selectedFilter = "ACABAT" },
                    label = { Text("Acabat") }
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cercar lot...") },
                singleLine = true,
                leadingIcon = { Text("🔍") }
            )

            Spacer(Modifier.height(8.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (errorMessage != null) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            } else if (filteredLots.isEmpty()) {
                Text(if (searchQuery.isBlank()) stringResource(R.string.lots_buit) else "No hi ha coincidències")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredLots) { lot ->
                        val estatColor = when (lot.estat) {
                            "OBERT" -> MaterialTheme.colorScheme.tertiary
                            "ACABAT" -> MaterialTheme.colorScheme.outline
                            else -> MaterialTheme.colorScheme.primary
                        }
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(lot.numLot.orEmpty(), fontWeight = FontWeight.Bold)
                                    Text(lot.estat.orEmpty(), color = estatColor, fontWeight = FontWeight.Bold)
                                }
                                Text(lot.catalogo?.nombre.orEmpty().ifEmpty { "-" })
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                    if (lot.estat == "EN_ESTOC") {
                                        Button(modifier = Modifier.padding(end = 8.dp), onClick = { onStartLot(lot.nifProveidor.orEmpty(), lot.idLot.orEmpty()) }) {
                                            Text(stringResource(R.string.lots_iniciar))
                                        }
                                    }
                                    if (lot.estat == "OBERT") {
                                        Button(onClick = { onFinishLot(lot.nifProveidor.orEmpty(), lot.idLot.orEmpty()) }) {
                                            Text(stringResource(R.string.lots_finalitzar))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
