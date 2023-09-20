package id.bts.plugins

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import id.bts.config.GlobalConfig
import io.ktor.server.application.*
import java.io.InputStream

fun Application.configureFirebase() {
  val fileSourceName = GlobalConfig.config.property("firebase_admin_sdk_file_name").getString()
  val serviceAccount: InputStream? = this::class.java.classLoader.getResourceAsStream(fileSourceName)
  val options: FirebaseOptions = FirebaseOptions.builder().setCredentials(
    GoogleCredentials.fromStream(
      serviceAccount
    )
  ).build()

  FirebaseApp.initializeApp(options)
}