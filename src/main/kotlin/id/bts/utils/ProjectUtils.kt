package id.bts.utils

import java.nio.file.Paths

object ProjectUtils {
  fun getProjectDir(deepPath: String = ""): String {
    return Paths.get(deepPath).toAbsolutePath().toString()
  }
}