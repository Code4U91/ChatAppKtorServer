package chatAppServer.functions

import chatAppServer.*
import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.api.core.ApiFuture
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


suspend fun handleCallNotification(request: CallNotificationRequest) {

    val senderName = getSenderName(request.senderId)
    val receiverTokens = getFcmTokens(request.receiverId)
    val profileImage = getSenderImage(request.senderId)

    val payload = mapOf(
        "type" to "call",
        "callId" to request.callId,
        "callType" to request.callType,
        "senderId" to request.senderId,
        "senderName" to senderName,
        "channelName" to request.channelName,
        "profileImage" to profileImage
    )

    val invalidTokens = sendMulticastNotification(receiverTokens, payload)

    removeInvalidFcmTokens(request.receiverId, invalidTokens)

}


suspend fun handleMessageNotification(request: MessageNotificationRequest) {


    val senderName = getSenderName(request.senderId)
    val messageBody = getMessageBody(request.chatId, request.messageId)
    val receiverTokens = getFcmTokens(request.receiverId)
    val profileImage = getSenderImage(request.senderId)
    val messageStatus = getMessageStatus(request.chatId, request.messageId)
    val activeChatId = getCurrentChattingWith(request.receiverId)

    val payload = mapOf(
        "type" to "message",
        "senderId" to request.senderId,
        "receiverId" to request.receiverId,
        "chatId" to request.chatId,
        "messageId" to request.messageId,
        "profileImage" to profileImage,
        "senderName" to senderName,
        "message" to messageBody
    )
    if (messageStatus != "seen" && activeChatId != request.chatId) {
        val invalidTokens = sendMulticastNotification(
            tokens = receiverTokens,
            data = payload

        )

        removeInvalidFcmTokens(request.receiverId, invalidTokens)
    }

}


fun sendMulticastNotification(
    tokens: List<String>,
    data: Map<String, String>
): List<String> {

    if (tokens.isEmpty()) return emptyList()

    val messageBuilder = MulticastMessage.builder()
        .putAllData(data)
        .addAllTokens(tokens)


    val response = FirebaseMessaging.getInstance().sendEachForMulticast(messageBuilder.build())

    val invalidTokens = mutableListOf<String>()

    response.responses.forEachIndexed { index, sendResponse ->

        if (!sendResponse.isSuccessful) {
            val errorCode = sendResponse.exception?.message

            if (errorCode?.contains("registration-token-not-registered") == true ||
                errorCode?.contains("invalid-registration-token") == true
            ) {

                invalidTokens.add(tokens[index])
            }
        }
    }
    return invalidTokens
}

suspend fun removeInvalidFcmTokens(userId: String, tokensToRemove: List<String>) {

    if (tokensToRemove.isEmpty()) return

    val db = FirestoreClient.getFirestore()
    val userRef = db.collection(USERS_COLLECTION).document(userId)

    val snapshot = userRef.get().await()

    @Suppress("UNCHECKED_CAST")
    val currentTokens = snapshot.get("fcmTokens") as? List<String> ?: return

    val updatedTokens = currentTokens.filterNot { tokensToRemove.contains(it) }

    userRef.update("fcmTokens", updatedTokens)
}


suspend fun getSenderName(senderId: String): String {
    val db = FirestoreClient.getFirestore()
    val doc = db.collection(USERS_COLLECTION).document(senderId).get().await()

    return doc.getString("name") ?: "Unknown"
}

suspend fun getMessageStatus(chatId: String, messageId: String): String {
    val db = FirestoreClient.getFirestore()
    val doc = db.collection(CHATS_COLLECTION).document(chatId).collection(MESSAGE_COLLECTION)
        .document(messageId).get().await()

    return doc.getString("Status") ?: "sent"

}

suspend fun getCurrentChattingWith(receiverId: String): String? = suspendCoroutine { cont ->

    val dbRef = FirebaseDatabase.getInstance()
        .getReference(USERS_REF)
        .child(receiverId)
        .child("currentChattingWith")

    dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val chatId = snapshot.getValue(String::class.java)
            cont.resume(chatId)
        }

        override fun onCancelled(error: DatabaseError) {
            cont.resume(null)
        }
    })
}


suspend fun getSenderImage(senderId: String): String {
    val db = FirestoreClient.getFirestore()
    val doc = db.collection(USERS_COLLECTION).document(senderId).get().await()

    return doc.getString("photoUrl")
        ?: "https://static.vecteezy.com/system/resources/thumbnails/020/765/399/small_2x/default-profile-account-unknown-icon-black-silhouette-free-vector.jpg"

}

suspend fun getMessageBody(chatId: String, messageId: String): String {
    val db = FirestoreClient.getFirestore()
    val doc = db.collection(CHATS_COLLECTION).document(chatId).collection(MESSAGE_COLLECTION)
        .document(messageId).get().await()

    return doc.getString("messageContent") ?: "New Message"

}

suspend fun getFcmTokens(userId: String): List<String> {

    val db = FirestoreClient.getFirestore()
    val doc = db.collection(USERS_COLLECTION).document(userId).get().await()

    @Suppress("UNCHECKED_CAST")
    return doc.get("fcmTokens") as? List<String> ?: emptyList()
}


suspend fun <T> ApiFuture<T>.await(): T = withContext(Dispatchers.IO) { get() }
