package cat.copernic.easytraza

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cat.copernic.easytraza.ui.navigation.AppNavigation
import cat.copernic.easytraza.ui.theme.EasyTrazaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { EasyTrazaTheme { AppNavigation() } }
    }
}
