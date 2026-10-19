package com.example.model

data class FileNode(
  val name: String,
  val isDirectory: Boolean,
  val path: String,
  val extension: String = "",
  val mimeType: String? = null,
  val sizeBytes: Long = 0L,
  val lastModified: Long = 0L,
  val children: List<FileNode>? = null
) {
  val isFile: Boolean get() = !isDirectory

  val childFilesCount: Int
    get() = children?.count { it.isFile } ?: 0

  val childFoldersCount: Int
    get() = children?.count { it.isDirectory } ?: 0

  val totalFilesCount: Int
    get() = (if (isFile) 1 else 0) + (children?.sumOf { it.totalFilesCount } ?: 0)

  val totalFoldersCount: Int
    get() = (if (isDirectory) 1 else 0) + (children?.sumOf { it.totalFoldersCount } ?: 0)

  val totalSizeBytes: Long
    get() = sizeBytes + (children?.sumOf { it.totalSizeBytes } ?: 0L)
}
