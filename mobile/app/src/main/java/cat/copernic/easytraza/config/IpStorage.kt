package cat.copernic.easytraza.config

import android.content.Context

/**
 * Classe per a la gestió de IpStorage a l'aplicació EasyTraza.
 */
class IpStorage {

    companion object {

        private const val PREF = "easytraza_config"
        private const val KEY_IP = "server_ip"

        fun saveIp(context: Context, ip: String) {
            val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            sp.edit().putString(KEY_IP, ip.trim()).apply()
        }

        fun getIp(context: Context): String {
            val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            return (sp.getString(KEY_IP, "192.168.1.85") ?: "192.168.1.85").trim()
        }
    }
}

