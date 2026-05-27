package cat.copernic.easytraza.ui.screens.config

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cat.copernic.easytraza.config.IpStorage
import cat.copernic.easytraza.R
import cat.copernic.easytraza.network.ConnectionService

/**
 * Classe per a la gestió de ConfigIpActivity a l'aplicació EasyTraza.
 */
class ConfigIpActivity : AppCompatActivity() {

    private lateinit var editIp: EditText
    private lateinit var btnSave: Button
    private lateinit var btnTest: Button
    private lateinit var tvResultado: TextView
    private lateinit var connectionService: ConnectionService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_ip)

        editIp = findViewById(R.id.editIp)
        btnSave = findViewById(R.id.btnSave)
        btnTest = findViewById(R.id.btnTest)
        tvResultado = findViewById(R.id.tvResultado)

        connectionService = ConnectionService(this)

        // Cargar IP guardada actualmente
        val ipActual = IpStorage.getIp(this)
        editIp.setText(ipActual)

        // Guardar IP
        btnSave.setOnClickListener {
            val ip = editIp.text.toString().trim()

            if (ip.isNotEmpty()) {
                IpStorage.saveIp(this, ip)
                Toast.makeText(this, "IP guardada: $ip", Toast.LENGTH_SHORT).show()
                tvResultado.text = "✅ IP guardada: $ip"
                tvResultado.visibility = android.view.View.VISIBLE
                tvResultado.setTextColor(Color.GREEN)
            } else {
                Toast.makeText(this, "La IP no puede estar vacía", Toast.LENGTH_SHORT).show()
            }
        }

        // Probar conexión
        btnTest.setOnClickListener {
            val ip = editIp.text.toString().trim()

            if (ip.isEmpty()) {
                Toast.makeText(this, "Introduce una IP primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Guardar temporalmente la IP para la prueba
            val ipOriginal = IpStorage.getIp(this)
            IpStorage.saveIp(this, ip)

            // Deshabilitar botón mientras se prueba
            btnTest.isEnabled = false
            btnTest.text = "Probando..."
            tvResultado.text = "🔄 Probando conexión con $ip..."
            tvResultado.visibility = android.view.View.VISIBLE
            tvResultado.setTextColor(Color.BLUE)

            connectionService.testConexion { resultado ->
                runOnUiThread {
                    tvResultado.text = resultado
                    btnTest.isEnabled = true
                    btnTest.text = "Probar conexión"

                    // Cambiar color según resultado
                    when {
                        resultado.contains("OK") -> tvResultado.setTextColor(Color.GREEN)
                        resultado.contains("ERROR") || resultado.contains("FAIL") -> tvResultado.setTextColor(Color.RED)
                        else -> tvResultado.setTextColor(Color.BLACK)
                    }

                    Toast.makeText(this@ConfigIpActivity, resultado, Toast.LENGTH_LONG).show()

                    // Restaurar IP original si no se guardó
                    if (!ip.equals(IpStorage.getIp(this@ConfigIpActivity))) {
                        IpStorage.saveIp(this@ConfigIpActivity, ipOriginal)
                    }
                }
            }
        }
    }
}