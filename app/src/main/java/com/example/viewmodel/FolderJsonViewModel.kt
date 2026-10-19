package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FolderScanner
import com.example.model.FileNode
import com.example.model.FilterMode
import com.example.model.IndentMode
import com.example.model.ScanFilterConfig
import com.example.model.ScanStatistics
import com.example.util.JsonTreeBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

sealed interface UiScanState {
  data object Idle : UiScanState
  data class Scanning(
    val foldersCount: Int = 0,
    val filesCount: Int = 0,
    val currentPath: String = ""
  ) : UiScanState
  data class Success(
    val rootNode: FileNode,
    val jsonContent: String,
    val statistics: ScanStatistics
  ) : UiScanState
  data class Error(val message: String) : UiScanState
}

sealed interface UiEvent {
  data class ShowToast(val message: String) : UiEvent
  data object CopySuccess : UiEvent
  data class SaveSuccess(val fileName: String) : UiEvent
  data class SaveError(val message: String) : UiEvent
}

class FolderJsonViewModel(application: Application) : AndroidViewModel(application) {

  private val scanner = FolderScanner(application)

  private val _scanState = MutableStateFlow<UiScanState>(UiScanState.Idle)
  val scanState: StateFlow<UiScanState> = _scanState.asStateFlow()

  private val _filterConfig = MutableStateFlow(ScanFilterConfig())
  val filterConfig: StateFlow<ScanFilterConfig> = _filterConfig.asStateFlow()

  private val _selectedUri = MutableStateFlow<Uri?>(null)
  val selectedUri: StateFlow<Uri?> = _selectedUri.asStateFlow()

  private val _selectedFolderName = MutableStateFlow<String?>(null)
  val selectedFolderName: StateFlow<String?> = _selectedFolderName.asStateFlow()

  private val _events = MutableSharedFlow<UiEvent>()
  val events: SharedFlow<UiEvent> = _events.asSharedFlow()

  private var scanJob: Job? = null
  private var lastRootNode: FileNode? = null
  private var lastStatistics: ScanStatistics? = null

  fun onFolderSelected(uri: Uri) {
    _selectedUri.value = uri
    try {
      // Take persistent permission if possible
      getApplication<Application>().contentResolver.takePersistableUriPermission(
        uri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION
      )
    } catch (e: Exception) {
      // Ignore if not supported
    }
    startScan()
  }

  fun startScan() {
    val uri = _selectedUri.value ?: return
    scanJob?.cancel()

    _scanState.value = UiScanState.Scanning()

    scanJob = viewModelScope.launch {
      try {
        val result = scanner.scanTree(
          treeUri = uri,
          config = _filterConfig.value,
          onProgress = { folders, files, path ->
            _scanState.update { current ->
              if (current is UiScanState.Scanning) {
                current.copy(foldersCount = folders, filesCount = files, currentPath = path)
              } else {
                UiScanState.Scanning(folders, files, path)
              }
            }
          }
        )

        lastRootNode = result.rootNode
        lastStatistics = result.statistics
        _selectedFolderName.value = result.statistics.rootFolderName

        val json = JsonTreeBuilder.buildJson(result.rootNode, _filterConfig.value)
        _scanState.value = UiScanState.Success(
          rootNode = result.rootNode,
          jsonContent = json,
          statistics = result.statistics
        )
      } catch (e: kotlinx.coroutines.CancellationException) {
        _scanState.value = UiScanState.Idle
      } catch (e: Exception) {
        _scanState.value = UiScanState.Error(
          e.localizedMessage ?: "خطا در خواندن فایل‌های پوشه رخ داد (Error scanning folder)"
        )
      }
    }
  }

  fun cancelScan() {
    scanJob?.cancel()
    _scanState.value = UiScanState.Idle
  }

  fun updateFilterConfig(newConfig: ScanFilterConfig) {
    val oldConfig = _filterConfig.value
    _filterConfig.value = newConfig

    // If only format/indent/metadata changed and we already have scanned data, regenerate JSON quickly
    val onlyFormattingChanged = oldConfig.filterMode == newConfig.filterMode &&
      oldConfig.extensions == newConfig.extensions &&
      oldConfig.includeHidden == newConfig.includeHidden &&
      oldConfig.includeEmptyFolders == newConfig.includeEmptyFolders &&
      oldConfig.maxDepth == newConfig.maxDepth

    val currentRoot = lastRootNode
    val currentStats = lastStatistics
    if (onlyFormattingChanged && currentRoot != null && currentStats != null && _scanState.value is UiScanState.Success) {
      val json = JsonTreeBuilder.buildJson(currentRoot, newConfig)
      _scanState.value = UiScanState.Success(
        rootNode = currentRoot,
        jsonContent = json,
        statistics = currentStats
      )
    } else if (_selectedUri.value != null && _scanState.value !is UiScanState.Scanning) {
      // Structural filter options changed, re-run scan
      startScan()
    }
  }

  fun setFilterMode(mode: FilterMode) {
    updateFilterConfig(_filterConfig.value.copy(filterMode = mode))
  }

  fun addExtension(ext: String) {
    val clean = ext.trim().lowercase().removePrefix(".")
    if (clean.isNotEmpty() && !clean.contains(" ")) {
      val newSet = _filterConfig.value.extensions + clean
      updateFilterConfig(_filterConfig.value.copy(extensions = newSet))
    }
  }

  fun removeExtension(ext: String) {
    val clean = ext.trim().lowercase().removePrefix(".")
    val newSet = _filterConfig.value.extensions - clean
    updateFilterConfig(_filterConfig.value.copy(extensions = newSet))
  }

  fun toggleIncludeHidden() {
    updateFilterConfig(_filterConfig.value.copy(includeHidden = !_filterConfig.value.includeHidden))
  }

  fun toggleIncludeEmptyFolders() {
    updateFilterConfig(_filterConfig.value.copy(includeEmptyFolders = !_filterConfig.value.includeEmptyFolders))
  }

  fun toggleIncludeMetadata() {
    updateFilterConfig(_filterConfig.value.copy(includeMetadata = !_filterConfig.value.includeMetadata))
  }

  fun toggleIncludeRelativePath() {
    updateFilterConfig(_filterConfig.value.copy(includeRelativePath = !_filterConfig.value.includeRelativePath))
  }

  fun setIndentMode(indentMode: IndentMode) {
    updateFilterConfig(_filterConfig.value.copy(indentMode = indentMode, isMinified = indentMode == IndentMode.COMPACT))
  }

  fun toggleMinified(isMinified: Boolean) {
    val newIndent = if (isMinified) IndentMode.COMPACT else if (_filterConfig.value.indentMode == IndentMode.COMPACT) IndentMode.TWO_SPACES else _filterConfig.value.indentMode
    updateFilterConfig(_filterConfig.value.copy(isMinified = isMinified, indentMode = newIndent))
  }

  fun setMaxDepth(depth: Int) {
    updateFilterConfig(_filterConfig.value.copy(maxDepth = depth))
  }

  fun copyJsonToClipboard(context: Context) {
    val currentState = _scanState.value
    if (currentState is UiScanState.Success) {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      val clip = ClipData.newPlainText("Folder Tree JSON", currentState.jsonContent)
      clipboard.setPrimaryClip(clip)
      viewModelScope.launch {
        _events.emit(UiEvent.CopySuccess)
      }
    }
  }

  fun saveJsonToDocument(destinationUri: Uri) {
    val currentState = _scanState.value
    if (currentState !is UiScanState.Success) return

    viewModelScope.launch {
      try {
        val context = getApplication<Application>()
        context.contentResolver.openOutputStream(destinationUri, "wt")?.use { outputStream ->
          OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
            writer.write(currentState.jsonContent)
            writer.flush()
          }
        }
        _events.emit(UiEvent.SaveSuccess("data.json"))
      } catch (e: Exception) {
        _events.emit(UiEvent.SaveError(e.localizedMessage ?: "خطا در ذخیره فایل data.json"))
      }
    }
  }

  fun shareJson(context: Context) {
    val currentState = _scanState.value
    if (currentState is UiScanState.Success) {
      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_TEXT, currentState.jsonContent)
        putExtra(Intent.EXTRA_SUBJECT, "data.json - Folder Structure")
      }
      val chooser = Intent.createChooser(intent, "اشتراک‌گذاری data.json (Share JSON)")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    }
  }
}
