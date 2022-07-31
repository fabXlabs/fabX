package cloud.fabX.fabXaccess.common

import java.net.URI

data class Config(
    val port: Int,
    val dbUrl: String,
    val dbUser: String,
    val dbPassword: String
) {
    companion object {
        fun fromEnv(): Config {
            val port = readEnvInt("PORT", 80)

            return System.getenv("DATABASE_URL").takeUnless { it.isNullOrEmpty() }
                ?.let {
                    val dbUri = URI(it)

                    val dbUrl = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path + "?sslmode=require"
                    val dbUser = dbUri.userInfo.split(":")[0]
                    val dbPassword = dbUri.userInfo.split(":")[1]

                    Config(port, dbUrl, dbUser, dbPassword)
                }
                ?: run {
                    val dbUrl = readEnvString("FABX_DB_URL", "jdbc:postgresql://localhost/postgres")
                    val dbUser = readEnvString("FABX_DB_USER", "postgres")
                    val dbPassword = readEnvString("FABX_DB_PASSWORD", "postgrespassword")

                    Config(port, dbUrl, dbUser, dbPassword)
                }
        }

        private fun readEnvString(name: String, default: String): String {
            return System.getenv(name).takeUnless { it.isNullOrEmpty() } ?: default
        }

        private fun readEnvInt(name: String, default: Int): Int {
            return System.getenv(name).takeUnless { it.isNullOrEmpty() }?.toIntOrNull() ?: default
        }
    }
}