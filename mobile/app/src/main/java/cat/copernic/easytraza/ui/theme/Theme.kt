package cat.copernic.easytraza.ui.theme


/** Definició del tema Material3 de l'aplicació EasyTraza. */
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BakeryBrown80,
    secondary = BakeryCream80,
    tertiary = BakeryOrange80
)

private val LightColorScheme = lightColorScheme(
    primary = BakeryBrown40,
    secondary = BakeryCream40,
    tertiary = BakeryOrange40,
    background = Color(0xFFFFF8F0),
    surface = Color(0xFFFFF8F0),
    onPrimary = Color.White,
    onSecondary = Color(0xFF4A3520),
    onTertiary = Color.White,
    onBackground = Color(0xFF2C1810),
    onSurface = Color(0xFF2C1810)
)

@Composable
fun EasyTrazaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
