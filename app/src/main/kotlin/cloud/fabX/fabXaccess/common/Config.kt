package cloud.fabX.fabXaccess.common

import java.net.URI
import kotlin.random.Random

data class Config(
    val port: Int,
    val dbUrl: String,
    val dbUser: String,
    val dbPassword: String,
    val jwtIssuer: String,
    val jwtAudience: String,
    val jwtHMAC256Secret: String,
    val webauthnOrigin: String,
    val webauthnRpId: String,
    val webauthnRpName: String,
    val cookieDomain: String,
    val cookiePath: String,
    val corsHost: String,
    val deviceReceiveTimeoutMillis: Long,
    val firmwareDirectory: String,
    val metricsPassword: String,
    val httpsRedirect: Boolean
) {
    companion object {
        fun fromEnv(): Config {
            val port = readEnvInt("PORT", 80)
            val jwtIssuer = readEnvString("JWT_ISSUER", "localhost")
            val jwtAudience = readEnvString("JWT_AUDIENCE", "localhost")
            val jwtHMAC256Secret = readEnvString("JWT_HMAC256_SECRET", Random.nextBytes(32).decodeToString())
            val webauthnOrigin = readEnvString("WEBAUTHN_ORIGIN", "http://localhost:4200")
            val webauthnRpId = readEnvString("WEBAUTHN_RP_ID", "localhost")
            val webauthnRpName = readEnvString("WEBAUTHN_RP_NAME", "fabX")
            val cookieDomain = readEnvString("COOKIE_DOMAIN", "")
            val cookiePath = readEnvString("COOKIE_PATH", "/")
            val corsHost = readEnvString("CORS_HOST", "localhost:5173")
            val deviceReceiveTimeoutMillis = readEnvLong("DEVICE_RECEIVE_TIMEOUT", 5000L)
            val firmwareDirectory = readEnvString("FIRMWARE_DIRECTORY", "/tmp/fabXfirmware")
            val metricsPassword = readEnvString("METRICS_PASSWORD", Random.nextBytes(32).decodeToString())
            val httpsRedirect = readEnvBoolean("HTTPS_REDIRECT", true)

            // TODO remove DATABASE_URL parsing (no longer used)
            return System.getenv("DATABASE_URL").takeUnless { it.isNullOrEmpty() }
                ?.let {
                    val dbUri = URI(it)

                    val dbUrl = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path + "?sslmode=require"
                    val dbUser = dbUri.userInfo.split(":")[0]
                    val dbPassword = dbUri.userInfo.split(":")[1]

                    Config(
                        port,
                        dbUrl,
                        dbUser,
                        dbPassword,
                        jwtIssuer,
                        jwtAudience,
                        jwtHMAC256Secret,
                        webauthnOrigin,
                        webauthnRpId,
                        webauthnRpName,
                        cookieDomain,
                        cookiePath,
                        corsHost,
                        deviceReceiveTimeoutMillis,
                        firmwareDirectory,
                        metricsPassword,
                        httpsRedirect
                    )
                }
                ?: run {
                    val dbUrl = readEnvString("FABX_DB_URL", "jdbc:postgresql://localhost/postgres")
                    val dbUser = readEnvString("FABX_DB_USER", "postgres")
                    val dbPassword = readEnvString("FABX_DB_PASSWORD", "postgrespassword")

                    Config(
                        port,
                        dbUrl,
                        dbUser,
                        dbPassword,
                        jwtIssuer,
                        jwtAudience,
                        jwtHMAC256Secret,
                        webauthnOrigin,
                        webauthnRpId,
                        webauthnRpName,
                        cookieDomain,
                        cookiePath,
                        corsHost,
                        deviceReceiveTimeoutMillis,
                        firmwareDirectory,
                        metricsPassword,
                        httpsRedirect
                    )
                }
        }

        private fun readEnvString(name: String, default: String): String {
            return System.getenv(name).takeUnless { it.isNullOrEmpty() } ?: default
        }

        private fun readEnvInt(name: String, default: Int): Int {
            return System.getenv(name).takeUnless { it.isNullOrEmpty() }?.toIntOrNull() ?: default
        }

        private fun readEnvLong(name: String, default: Long): Long {
            return System.getenv(name).takeUnless { it.isNullOrEmpty() }?.toLongOrNull() ?: default
        }

        private fun readEnvBoolean(name: String, default: Boolean): Boolean {
            return System.getenv(name).takeUnless { it.isNullOrEmpty() }?.toBoolean() ?: default
        }
    }
}