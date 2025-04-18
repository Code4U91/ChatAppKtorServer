package chatAppServer.routing

import chatAppServer.CallNotificationRequest
import chatAppServer.FirebaseUidKey
import chatAppServer.fcm.FirebaseUtil
import chatAppServer.functions.handleCallNotification
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.callRoutes() {
    FirebaseUtil.initFirebase()

    routing {

        post("/sendCallNotification") {

            val uid = call.attributes[FirebaseUidKey]
            val request = call.receive<CallNotificationRequest>()

            if (uid != request.senderId) {
                call.respond(HttpStatusCode.Forbidden, "SenderId does not match authenticated user")
                return@post
            }
            handleCallNotification(request)
            call.respond(HttpStatusCode.OK)
        }
    }
}