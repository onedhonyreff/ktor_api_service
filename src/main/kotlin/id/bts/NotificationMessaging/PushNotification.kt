package id.bts.NotificationMessaging

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object PushNotification {
  inline fun <reified T : Any> sendMessage(
    title: String,
    message: String,
    tokenList: List<String>,
    topic: String? = null,
    data: T? = null
  ) {
    tokenList.forEach {
      val messageBuilder = Message.builder()
        .setNotification(
          com.google.firebase.messaging.Notification.builder()
            .setTitle(title)
            .setBody(message)
            .build()
        )

      data?.let {
        val stringJSON = Json.encodeToString(data)
        val dataMap = hashMapOf("additional_data" to stringJSON)
        messageBuilder.putAllData(dataMap)
      }
      messageBuilder.setToken(it)
      messageBuilder.setTopic(topic)

      val firebaseMessage = messageBuilder.build()
      FirebaseMessaging.getInstance().send(firebaseMessage)
    }
  }
}