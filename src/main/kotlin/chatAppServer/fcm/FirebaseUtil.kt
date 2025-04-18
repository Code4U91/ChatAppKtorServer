package chatAppServer.fcm

import chatAppServer.FIREBASE_ADMIN_JSON
import chatAppServer.FIREBASE_DATABASE_URL
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*


object FirebaseUtil {

    fun initFirebase() {
        if (FirebaseApp.getApps().isEmpty()) {

            val firebaseAdminJsonBase64 = System.getenv(FIREBASE_ADMIN_JSON)
            if (firebaseAdminJsonBase64.isNullOrEmpty()) {
                throw IllegalStateException("Firebase Admin JSON is missing in environment variables.")
            }

            val decodedJson = String(Base64.getDecoder().decode(firebaseAdminJsonBase64))
            val serviceAccount: InputStream = ByteArrayInputStream(decodedJson.toByteArray())

            val firebaseDatabaseUrl = System.getenv(FIREBASE_DATABASE_URL)

            if (firebaseDatabaseUrl.isNullOrEmpty()) {
                throw IllegalStateException("Firebase Database URl error.")
            }
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl(firebaseDatabaseUrl)
                .build()

            FirebaseApp.initializeApp(options)
        }


    }
}
