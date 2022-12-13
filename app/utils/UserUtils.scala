package utils

import java.security.MessageDigest

object UserUtils {
  def passwordHash(password: String) =
    MessageDigest.getInstance("SHA-256").digest(password.getBytes).map("%02x".format(_)).mkString
}
