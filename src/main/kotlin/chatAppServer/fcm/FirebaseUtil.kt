package chatAppServer.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.FileInputStream


object FirebaseUtil {

    fun initFirebase() {
        if (FirebaseApp.getApps().isEmpty()) {
            val serviceAccount = FileInputStream("secrets/serviceAccountKey.json")
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://chatsapp-5407a-default-rtdb.firebaseio.com/")
                .build()

            FirebaseApp.initializeApp(options)
        }


    }
}
