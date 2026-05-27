package cat.copernic.easytraza.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.easytraza.R
import cat.copernic.easytraza.config.IpStorage
import cat.copernic.easytraza.network.RetrofitClient
import cat.copernic.easytraza.ui.screens.config.ConfigIpScreen
import cat.copernic.easytraza.ui.screens.albaran.OcrScreen
import cat.copernic.easytraza.ui.screens.albarans.AlbaransScreen
import cat.copernic.easytraza.ui.viewmodels.albarans.AlbaransViewModel
import cat.copernic.easytraza.ui.screens.login.LoginScreen
import cat.copernic.easytraza.ui.viewmodels.login.LoginViewModel
import cat.copernic.easytraza.ui.screens.lots.LotsScreen
import cat.copernic.easytraza.ui.viewmodels.lots.LotsViewModel

enum class AppScreen { LOGIN, HOME, OCR, LOTS, ALBARANS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val context = LocalContext.current
    var ipActual by remember { mutableStateOf(IpStorage.getIp(context)) }
    var mostrarConfig by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }
    var usuarioLogeado by remember { mutableStateOf<cat.copernic.easytraza.model.Usuari?>(null) }

    val loginVM = remember(ipActual) { LoginViewModel(ipActual) }
    val lotsVM = remember(ipActual) { LotsViewModel(ipActual) }
    val albaransVM = remember(ipActual) { AlbaransViewModel(ipActual) }

    val loginState by loginVM.state.collectAsState()
    val lotsState by lotsVM.state.collectAsState()

    fun tancarSessio() {
        loginVM.tancarSessio()
        usuarioLogeado = null
        currentScreen = AppScreen.LOGIN
    }

    LaunchedEffect(ipActual, currentScreen) {
        if (currentScreen == AppScreen.LOGIN && loginState.usuarios.isEmpty()) loginVM.carregarUsuaris()
    }

    if (loginState.usuarioLogeado != null && currentScreen == AppScreen.LOGIN) {
        usuarioLogeado = loginState.usuarioLogeado
        currentScreen = AppScreen.HOME
    }

    if (mostrarConfig) {
        ConfigIpScreen(
            onBack = {
                val novaIp = IpStorage.getIp(context)
                if (novaIp != ipActual) {
                    ipActual = novaIp
                    loginVM.tancarSessio()
                    usuarioLogeado = null
                    currentScreen = AppScreen.LOGIN
                } else {
                    mostrarConfig = false
                }
            }
        )
        return
    }

    if (currentScreen == AppScreen.LOGIN) {
        if (loginState.isLoggingIn) {
            Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }) { pv ->
                Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.login_iniciando))
                    }
                }
            }
        } else {
            LoginScreen(
                usuarios = loginState.usuarios, isLoading = loginState.isLoading,
                errorMessage = loginState.error, ipActual = ipActual,
                onUsuarioSelected = { loginVM.ferLogin(it) },
                onConfigIp = { mostrarConfig = true }
            )
        }
        return
    }

    val bottomScreens = listOf(
        AppScreen.HOME to stringResource(R.string.bottom_inici) to Icons.Default.Home,
        AppScreen.OCR to stringResource(R.string.bottom_ocr) to Icons.Default.Search,
        AppScreen.LOTS to stringResource(R.string.bottom_lots) to Icons.Default.List,
        AppScreen.ALBARANS to stringResource(R.string.bottom_albarans) to Icons.Default.Email
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomScreens.forEach { (screenInfo, icon) ->
                    val (screen, label) = screenInfo
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = {
                            if (currentScreen != screen) {
                                currentScreen = screen
                                if (screen == AppScreen.LOTS) lotsVM.carregarLots()
                                if (screen == AppScreen.ALBARANS) albaransVM.carregarAlbarans()
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (currentScreen) {
                AppScreen.HOME -> {
                    val loggedUser = usuarioLogeado
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text(stringResource(R.string.app_name)) },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary),
                                actions = {
                                    if (loggedUser != null) TextButton(onClick = { tancarSessio() }) {
                                        Text(stringResource(R.string.sortir), color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp)
                                    }
                                }
                            )
                        }
                    ) { pv ->
                        Column(Modifier.fillMaxSize().padding(pv).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (loggedUser != null) {
                                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(stringResource(R.string.home_usuari, loggedUser.nombre), style = MaterialTheme.typography.titleMedium)
                                        Text(stringResource(R.string.home_rol, loggedUser.rol), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { currentScreen = AppScreen.OCR }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text(stringResource(R.string.btn_ocr), fontSize = 16.sp) }
                            Button(onClick = { currentScreen = AppScreen.LOTS; lotsVM.carregarLots() }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) { Text(stringResource(R.string.btn_lots), fontSize = 16.sp) }
                            Button(onClick = { currentScreen = AppScreen.ALBARANS; albaransVM.carregarAlbarans() }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text(stringResource(R.string.btn_albarans), fontSize = 16.sp) }
                            Button(onClick = { mostrarConfig = true }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)) { Text(stringResource(R.string.btn_config_ip), fontSize = 16.sp) }
                        }
                    }
                }

                AppScreen.OCR -> OcrScreen(usuarioId = usuarioLogeado?.id ?: 1L, onBack = { currentScreen = AppScreen.HOME })
                AppScreen.LOTS -> LotsScreen(lots = lotsState.lots, isLoading = lotsState.isLoading, errorMessage = lotsState.error, onRefresh = { lotsVM.carregarLots() }, onStartLot = { n, i -> lotsVM.canviarEstat(n, i, "OBERT") }, onFinishLot = { n, i -> lotsVM.canviarEstat(n, i, "ACABAT") }, onBack = { currentScreen = AppScreen.HOME })
                AppScreen.ALBARANS -> {
                    val albState by albaransVM.state.collectAsState()
                    AlbaransScreen(ip = ipActual, albarans = albState.albarans, isLoading = albState.isLoading, error = albState.error, onBack = { currentScreen = AppScreen.HOME }, onRefresh = { albaransVM.carregarAlbarans() })
                }
                else -> {}
            }
        }
    }
}
