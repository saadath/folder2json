package com.example.model

data class ScanStatistics(
  val rootFolderName: String,
  val totalFilesScanned: Int,
  val totalFoldersScanned: Int,
  val includedFiles: Int,
  val includedFolders: Int,
  val filteredOutFiles: Int,
  val totalSizeBytes: Long,
  val scanDurationMs: Long
)
