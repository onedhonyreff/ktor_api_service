package id.bts.utils

import java.nio.file.Paths

object ProjectUtil {
  fun getProjectDir(deepPath: String = ""): String {
    return Paths.get(deepPath).toAbsolutePath().toString()
  }
}