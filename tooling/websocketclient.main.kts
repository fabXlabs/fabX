@file:DependsOn("io.ktor:ktor-client-core-jvm:1.6.7")
@file:DependsOn("io.ktor:ktor-client-auth-jvm:1.6.7")
@file:DependsOn("io.ktor:ktor-client-cio-jvm:1.6.7")
@file:DependsOn("io.ktor:ktor-client-websockets-jvm:1.6.7")
@file:DependsOn("com.beust:klaxon:5.6")

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.BasicAuthCredentials
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.DefaultWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readBytes
import io.ktor.http.cio.websocket.readReason
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import java.lang.StringBuilder
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

val client = HttpClient(CIO) {
    install(WebSockets)
    install(Auth) {
        basic {
            credentials {
                BasicAuthCredentials(username = "AABBCC000000", password = "a2a50c75e271104dcbaeda71c2e9a7fc")
            }
        }
    }
}

suspend fun DefaultWebSocketSession.outputMessages() {
    try {
        for (frame in incoming) {
            try {
                when (frame) {
                    is Frame.Binary -> println("Binary: ${frame.readBytes()}")
                    is Frame.Text -> {
                        val text = frame.readText()
                        println("Text: $text")

                        val parser: Parser = Parser.default()
                        val json: JsonObject = parser.parse(StringBuilder(text)) as JsonObject

                        if (json.string("type") == "cloud.fabX.fabXaccess.device.ws.UnlockTool") {
                            val commandId = json.long("commandId")!!
                            val response = "{" +
                                    "\"type\":\"cloud.fabX.fabXaccess.device.ws.ToolUnlockResponse\"," +
                                    "\"commandId\":$commandId" +
                                    "}"
                            println("Sending response: $response")
                            try {
                                send(response)
                            } catch (e: Exception) {
                                println("error while sending response: " + e.localizedMessage)
                                e.printStackTrace()
                            }
                        }
                    }

                    is Frame.Close -> println("Close: ${frame.readReason()}")
                    is Frame.Ping -> println("Ping: ${frame.readBytes()}")
                    is Frame.Pong -> println("Pong: ${frame.readBytes()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("inner error while receiving: " + e.localizedMessage)
            }
        }
    } catch (e: Exception) {
        println("outer error while receiving: " + e.localizedMessage)
    }
}

suspend fun DefaultWebSocketSession.inputMessages() {
    while (true) {
        val message = readLine()
        if (message != null && message.isNotEmpty()) {
            try {
                send(message)
            } catch (e: Exception) {
                println("error while sending: " + e.localizedMessage)
            }
        } else {
            return
        }
    }
}

runBlocking {
    client.webSocket(method = HttpMethod.Get, host = "localhost", port = 8080, path = "/api/v1/device/ws") {
        println("connection established")
        val messageOutputRoutine = launch { outputMessages() }
        val messageInputRoutine = launch { inputMessages() }

        messageInputRoutine.join()
        messageOutputRoutine.cancelAndJoin()
    }
}
client.close()
println("connection closed")