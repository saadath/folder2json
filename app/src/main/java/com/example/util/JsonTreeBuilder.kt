package com.example.util

import com.example.model.FileNode
import com.example.model.IndentMode
import com.example.model.ScanFilterConfig
import org.json.JSONArray
import org.json.JSONObject

object JsonTreeBuilder {

  /**
   * Converts a FileNode tree into a JSON String matching the exact directory hierarchy.
   */
  fun buildJson(rootNode: FileNode, config: ScanFilterConfig): String {
    val rootJson = nodeToJsonObject(rootNode, config)
    if (config.isMinified) {
      return rootJson.toString()
    }
    return when (config.indentMode) {
      IndentMode.TWO_SPACES -> rootJson.toString(2)
      IndentMode.FOUR_SPACES -> rootJson.toString(4)
      IndentMode.COMPACT -> rootJson.toString()
    }
  }

  private fun nodeToJsonObject(node: FileNode, config: ScanFilterConfig): JSONObject {
    val obj = JSONObject()

    obj.put("name", node.name)
    obj.put("type", if (node.isDirectory) "directory" else "file")

    if (config.includeRelativePath) {
      obj.put("path", node.path)
    }

    if (node.isDirectory) {
      if (config.includeMetadata) {
        obj.put("childFileCount", node.childFilesCount)
        obj.put("childDirectoryCount", node.childFoldersCount)
        obj.put("totalFiles", node.totalFilesCount)
        obj.put("totalDirectories", (node.totalFoldersCount - 1).coerceAtLeast(0))
        obj.put("totalSizeBytes", node.totalSizeBytes)
        obj.put("totalSizeFormatted", FormatUtils.formatFileSize(node.totalSizeBytes))
        if (node.lastModified > 0) {
          obj.put("lastModified", FormatUtils.formatIsoTimestamp(node.lastModified))
        }
      }

      val childrenArray = JSONArray()
      node.children?.forEach { child ->
        childrenArray.put(nodeToJsonObject(child, config))
      }
      obj.put("children", childrenArray)
    } else {
      if (node.extension.isNotEmpty()) {
        obj.put("extension", node.extension)
      }

      if (config.includeMetadata) {
        if (!node.mimeType.isNullOrBlank()) {
          obj.put("mimeType", node.mimeType)
        }
        obj.put("sizeBytes", node.sizeBytes)
        obj.put("sizeFormatted", FormatUtils.formatFileSize(node.sizeBytes))
        if (node.lastModified > 0) {
          obj.put("lastModified", FormatUtils.formatIsoTimestamp(node.lastModified))
        }
      }
    }

    return obj
  }
}
