package com.example.model

enum class FilterMode {
  ALL_FILES,
  ONLY_SPECIFIED,
  EXCLUDE_SPECIFIED
}

enum class IndentMode(val spaces: Int, val label: String) {
  TWO_SPACES(2, "۲ فاصله (2 Spaces)"),
  FOUR_SPACES(4, "۴ فاصله (4 Spaces)"),
  COMPACT(0, "فشرده (Minified)")
}

data class ScanFilterConfig(
  val filterMode: FilterMode = FilterMode.ALL_FILES,
  val extensions: Set<String> = setOf("txt", "pdf", "jpg", "png", "json", "docx", "kt", "js"),
  val includeHidden: Boolean = false,
  val includeEmptyFolders: Boolean = true,
  val includeMetadata: Boolean = true,
  val includeRelativePath: Boolean = true,
  val isMinified: Boolean = false, // false = Pretty-printed, true = Minified
  val indentMode: IndentMode = IndentMode.TWO_SPACES,
  val maxDepth: Int = 0 // 0 means unlimited
) {
  val isPrettyPrinted: Boolean get() = !isMinified

  /**
   * Checks if a file with given name and extension should be included according to this configuration.
   */
  fun shouldIncludeFile(name: String, extension: String): Boolean {
    if (!includeHidden && name.startsWith(".")) {
      return false
    }

    val cleanExt = extension.lowercase().removePrefix(".")
    return when (filterMode) {
      FilterMode.ALL_FILES -> true
      FilterMode.ONLY_SPECIFIED -> {
        extensions.any { it.equals(cleanExt, ignoreCase = true) }
      }
      FilterMode.EXCLUDE_SPECIFIED -> {
        !extensions.any { it.equals(cleanExt, ignoreCase = true) }
      }
    }
  }

  fun shouldIncludeDirectory(name: String, depth: Int): Boolean {
    if (!includeHidden && name.startsWith(".")) {
      return false
    }
    if (maxDepth > 0 && depth > maxDepth) {
      return false
    }
    return true
  }
}
