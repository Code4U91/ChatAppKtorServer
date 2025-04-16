package chatAppServer

import chatAppServer.functions.await
import com.google.firebase.auth.FirebaseAuth
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

fun Application.configureFirebaseAuth()
{
    intercept(ApplicationCallPipeline.Plugins){

        val authHeader = call.request.headers["Authorization"]
        val uid = verifyFirebaseToken(authHeader)
        if (uid == null)
        {
            call.respond(HttpStatusCode.Unauthorized, "Missing or Invalid firebase token")
            finish()
        } else {
            call.attributes.put(FirebaseUidKey, uid)
        }
    }
}

suspend fun verifyFirebaseToken(authHeader: String?): String? {

    if (authHeader == null || !authHeader.startsWith("Bearer ")) return null

    val idToken = authHeader.removePrefix("Bearer ").trim()

    return try {
        val decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(idToken).await()
        decodedToken.uid
    } catch (e: Exception){
        println("X Firebase token verification failed : ${e.message}")
        null
    }
}
