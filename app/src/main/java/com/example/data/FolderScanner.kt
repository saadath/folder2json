package com.example.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.example.model.FileNode
import com.example.model.ScanFilterConfig
import com.example.model.ScanStatistics
import com.example.util.FormatUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class FolderScanner(private val context: Context) {

  private val contentResolver: ContentResolver = context.contentResolver

  data class ScanResult(
    val rootNode: FileNode,
    val statistics: ScanStatistics
  )

  suspend fun scanTree(
    treeUri: Uri,
    config: ScanFilterConfig,
    onProgress: (foldersCount: Int, filesCount: Int, currentPath: String) -> Unit
  ): ScanResult = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()

    val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)
    val rootName = getDocumentDisplayName(treeUri, rootDocId) ?: "Root"

    var totalFilesCount = 0
    var totalFoldersCount = 1 // Root folder
    var includedFilesCount = 0
    var filteredOutCount = 0

    suspend fun traverse(
      docId: String,
      dirName: String,
      currentPath: String,
      currentDepth: Int
    ): FileNode? {
      coroutineContext.ensureActive()

      if (!config.shouldIncludeDirectory(dirName, currentDepth)) {
        return null
      }

      onProgress(totalFoldersCount, totalFilesCount, currentPath)

      val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)
      val projection = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_SIZE,
        DocumentsContract.Document.COLUMN_LAST_MODIFIED
      )

      val childNodes = mutableListOf<FileNode>()

      try {
        contentResolver.query(
          childrenUri,
          projection,
          null,
          null,
          null
        )?.use { cursor ->
          val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
          val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
          val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
          val sizeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
          val modCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

          while (cursor.moveToNext()) {
            coroutineContext.ensureActive()

            val childDocId = if (idCol >= 0) cursor.getString(idCol) ?: continue else continue
            val childName = if (nameCol >= 0) cursor.getString(nameCol) ?: "unknown" else "unknown"
            val mimeType = if (mimeCol >= 0) cursor.getString(mimeCol) else null
            val size = if (sizeCol >= 0 && !cursor.isNull(sizeCol)) cursor.getLong(sizeCol) else 0L
            val lastModified = if (modCol >= 0 && !cursor.isNull(modCol)) cursor.getLong(modCol) else 0L

            val isDir = mimeType == DocumentsContract.Document.MIME_TYPE_DIR
            val childPath = if (currentPath == "/") "/$childName" else "$currentPath/$childName"

            if (isDir) {
              totalFoldersCount++
              val subDirNode = traverse(
                docId = childDocId,
                dirName = childName,
                currentPath = childPath,
                currentDepth = currentDepth + 1
              )
              if (subDirNode != null) {
                if (config.includeEmptyFolders || subDirNode.totalFilesCount > 0) {
                  childNodes.add(subDirNode)
                }
              }
            } else {
              totalFilesCount++
              val ext = FormatUtils.getFileExtension(childName)
              if (config.shouldIncludeFile(childName, ext)) {
                includedFilesCount++
                childNodes.add(
                  FileNode(
                    name = childName,
                    isDirectory = false,
                    path = childPath,
                    extension = ext,
                    mimeType = mimeType,
                    sizeBytes = size,
                    lastModified = lastModified,
                    children = null
                  )
                )
              } else {
                filteredOutCount++
              }
            }

            if ((totalFilesCount + totalFoldersCount) % 10 == 0) {
              onProgress(totalFoldersCount, totalFilesCount, childPath)
            }
          }
        }
      } catch (e: Exception) {
        // Continue gracefully if a subdirectory cannot be read
      }

      // Sort children: directories first, then alphabetical
      val sortedChildren = childNodes.sortedWith(
        compareBy<FileNode> { !it.isDirectory }
          .thenBy { it.name.lowercase() }
      )

      return FileNode(
        name = dirName,
        isDirectory = true,
        path = currentPath,
        extension = "",
        mimeType = DocumentsContract.Document.MIME_TYPE_DIR,
        sizeBytes = 0L,
        lastModified = 0L,
        children = sortedChildren
      )
    }

    val rootPath = "/$rootName"
    val rootNode = traverse(
      docId = rootDocId,
      dirName = rootName,
      currentPath = rootPath,
      currentDepth = 0
    ) ?: FileNode(
      name = rootName,
      isDirectory = true,
      path = rootPath,
      children = emptyList()
    )

    val duration = System.currentTimeMillis() - startTime
    val stats = ScanStatistics(
      rootFolderName = rootName,
      totalFilesScanned = totalFilesCount,
      totalFoldersScanned = totalFoldersCount,
      includedFiles = includedFilesCount,
      includedFolders = rootNode.totalFoldersCount,
      filteredOutFiles = filteredOutCount,
      totalSizeBytes = rootNode.totalSizeBytes,
      scanDurationMs = duration
    )

    ScanResult(rootNode = rootNode, statistics = stats)
  }

  private fun getDocumentDisplayName(treeUri: Uri, documentId: String): String? {
    return try {
      val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
      contentResolver.query(
        docUri,
        arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
        null,
        null,
        null
      )?.use { cursor ->
        if (cursor.moveToFirst()) {
          val colIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
          if (colIdx >= 0) cursor.getString(colIdx) else null
        } else {
          null
        }
      }
    } catch (e: Exception) {
      null
    }
  }
}
