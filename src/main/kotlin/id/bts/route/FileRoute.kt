package id.bts.route

import id.bts.utils.Extensions.returnNotFoundResponse
import id.bts.utils.Extensions.returnParameterErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureFileRouting() {
  routing {
    get("/file/{fileAction}/{mediaType}/{contentType}/{fileName}") {
      val fileAction = call.parameters["fileAction"]
      val mediaType = call.parameters["mediaType"]
      val contentType = call.parameters["contentType"]
      val fileName = call.parameters["fileName"]
      val file = File("assets/${mediaType}s/$contentType/$fileName")

      if (!file.exists()) {
        call.returnNotFoundResponse()
      } else {
        when (fileAction) {
          "open" -> {
            call.response.header(
              HttpHeaders.ContentDisposition,
              ContentDisposition.Inline.toString()
            )
          }

          "download" -> {
            call.response.header(
              HttpHeaders.ContentDisposition,
              ContentDisposition.Attachment.withParameter(
                ContentDisposition.Parameters.FileName, "${System.currentTimeMillis()}.${file.extension}"
              ).toString()
            )
          }

          else -> {
            call.returnParameterErrorResponse("Second url parameter <${fileAction}> is not allowed. Make sure it is either <open> or <download>")
            return@get
          }
        }

        call.respondFile(file)
      }
    }
  }
}