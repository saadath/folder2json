package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

object FormatUtils {

  fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    val value = bytes / 1024.0.pow(digitGroups.toDouble())
    return if (digitGroups == 0) {
      "$bytes B"
    } else {
      String.format(Locale.US, "%.2f %s", value, units[digitGroups])
    }
  }

  fun formatIsoTimestamp(millis: Long): String {
    if (millis <= 0L) return ""
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
    return sdf.format(Date(millis))
  }

  fun formatReadableDate(millis: Long): String {
    if (millis <= 0L) return "-"
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
  }

  fun getFileExtension(filename: String): String {
    val lastDot = filename.lastIndexOf('.')
    return if (lastDot >= 0 && lastDot < filename.length - 1) {
      filename.substring(lastDot + 1).lowercase()
    } else {
      ""
    }
  }

  enum class FileCategory {
    FOLDER,
    CODE,
    IMAGE,
    AUDIO,
    VIDEO,
    DOCUMENT,
    ARCHIVE,
    DATA,
    OTHER
  }

  fun getFileCategory(extension: String): FileCategory {
    return when (extension.lowercase()) {
      "kt", "java", "py", "js", "ts", "html", "css", "c", "cpp", "h", "go", "rs", "php", "rb", "swift", "dart", "sh" -> FileCategory.CODE
      "jpg", "jpeg", "png", "webp", "gif", "svg", "bmp", "ico", "heic" -> FileCategory.IMAGE
      "mp3", "wav", "m4a", "flac", "ogg", "aac" -> FileCategory.AUDIO
      "mp4", "mkv", "avi", "mov", "webm", "flv", "wmv" -> FileCategory.VIDEO
      "pdf", "doc", "docx", "txt", "md", "rtf", "odt", "xls", "xlsx", "ppt", "pptx" -> FileCategory.DOCUMENT
      "zip", "rar", "7z", "tar", "gz", "bz2" -> FileCategory.ARCHIVE
      "json", "xml", "yaml", "yml", "csv", "tsv", "sql", "db", "sqlite" -> FileCategory.DATA
      else -> FileCategory.OTHER
    }
  }
}
