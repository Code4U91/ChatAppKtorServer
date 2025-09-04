package chatAppServer.routing

import chatAppServer.FirebaseUidKey
import chatAppServer.MessageNotificationRequest
import chatAppServer.functions.handleMessageNotification
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.messageRoutes() {

    routing {

        post("/sendMessageNotification") {

            val uid = call.attributes[FirebaseUidKey]
            val request = call.receive<MessageNotificationRequest>()

            if (uid != request.senderId) {
                call.respond(HttpStatusCode.Forbidden, "SenderId does not match authenticated user")
                return@post
            }

            handleMessageNotification(request)
            call.respond(HttpStatusCode.OK)
        }
    }
}




