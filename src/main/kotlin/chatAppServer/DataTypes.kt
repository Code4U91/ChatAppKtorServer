package chatAppServer

import io.ktor.util.*
import kotlinx.serialization.Serializable

val FirebaseUidKey = AttributeKey<String>("firebaseUid")

@Serializable
data class MessageNotificationRequest(
    val senderId: String,
    val receiverId: String,
    val messageId: String,
    val chatId: String

)

@Serializable
data class CallNotificationRequest(
    val callId: String,
    val channelName: String,
    val callType: String,
    val senderId: String,
    val receiverId: String
)

