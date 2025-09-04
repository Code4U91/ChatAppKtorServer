package chatAppServer.server

import chatAppServer.configureFirebaseAuth
import chatAppServer.fcm.FirebaseUtil
import chatAppServer.routing.callRoutes
import chatAppServer.routing.messageRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun main() {

    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    FirebaseUtil.initFirebase()

    embeddedServer(Netty, port = port, host = "0.0.0.0")
    {
        install(CallLogging)
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        configureFirebaseAuth()

        messageRoutes()
        callRoutes()
    }.start(wait = true)
}