package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FileTreeViewer
import com.example.ui.components.FilterSettingsView
import com.example.ui.components.JsonViewer
import com.example.ui.components.StatsCard
import com.example.viewmodel.FolderJsonViewModel
import com.example.viewmodel.UiEvent
import com.example.viewmodel.UiScanState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderJsonScreen(
  viewModel: FolderJsonViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scanState by viewModel.scanState.collectAsStateWithLifecycle()
  val filterConfig by viewModel.filterConfig.collectAsStateWithLifecycle()
  val selectedUri by viewModel.selectedUri.collectAsStateWithLifecycle()
  val selectedFolderName by viewModel.selectedFolderName.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }
  var selectedTabIndex by remember { mutableIntStateOf(0) }

  // SAF Folder Picker launcher
  val folderPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocumentTree()
  ) { uri: Uri? ->
    uri?.let { viewModel.onFolderSelected(it) }
  }

  // SAF CreateDocument launcher for saving data.json
  val saveFileLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
  ) { uri: Uri? ->
    uri?.let { viewModel.saveJsonToDocument(it) }
  }

  // Listen to UI Events (Copy success, Save success, Toast)
  LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
      when (event) {
        is UiEvent.CopySuccess -> {
          snackbarHostState.showSnackbar("متن JSON با موفقیت در کلیپ‌بورد کپی شد (Copied to Clipboard)")
        }
        is UiEvent.SaveSuccess -> {
          snackbarHostState.showSnackbar("فایل ${event.fileName} با موفقیت ذخیره شد (Saved successfully)")
        }
        is UiEvent.SaveError -> {
          snackbarHostState.showSnackbar(event.message)
        }
        is UiEvent.ShowToast -> {
          snackbarHostState.showSnackbar(event.message)
        }
      }
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.DataObject,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "Folder2JSON",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "تبدیل فولدر به دیتای درختی JSON",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        },
        actions = {
          if (selectedUri != null) {
            IconButton(
              onClick = { viewModel.startScan() },
              modifier = Modifier.testTag("rescan_top_button")
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Rescan",
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }
          IconButton(
            onClick = { folderPickerLauncher.launch(null) },
            modifier = Modifier.testTag("select_folder_top_button")
          ) {
            Icon(
              imageVector = Icons.Default.FolderOpen,
              contentDescription = "Select Folder",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    floatingActionButton = {
      if (scanState !is UiScanState.Scanning) {
        ExtendedFloatingActionButton(
          onClick = { folderPickerLauncher.launch(null) },
          icon = { Icon(Icons.Default.FolderOpen, contentDescription = "Select Folder") },
          text = {
            Text(
              text = if (selectedUri == null) "انتخاب فولدر (Select)" else "انتخاب فولدر دیگر",
              fontWeight = FontWeight.Bold
            )
          },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          modifier = Modifier.testTag("fab_select_folder")
        )
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (val state = scanState) {
        is UiScanState.Idle -> {
          EmptyFolderState(
            onSelectFolderClicked = { folderPickerLauncher.launch(null) }
          )
        }

        is UiScanState.Scanning -> {
          ScanningProgressState(
            scanning = state,
            onCancelClicked = { viewModel.cancelScan() }
          )
        }

        is UiScanState.Error -> {
          ErrorScanState(
            message = state.message,
            onRetryClicked = { viewModel.startScan() },
            onSelectNewFolderClicked = { folderPickerLauncher.launch(null) }
          )
        }

        is UiScanState.Success -> {
          // Success State with Summary & Navigation Tabs
          Column(modifier = Modifier.fillMaxSize()) {
            // Stats Card at top
            StatsCard(
              statistics = state.statistics,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Primary Tabs
            PrimaryTabRow(
              selectedTabIndex = selectedTabIndex,
              containerColor = MaterialTheme.colorScheme.surface,
              contentColor = MaterialTheme.colorScheme.primary,
              modifier = Modifier.fillMaxWidth()
            ) {
              Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("کد JSON (data.json)", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Code, contentDescription = "JSON") },
                modifier = Modifier.testTag("tab_json")
              )
              Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("نمای درختی (Tree)", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AccountTree, contentDescription = "Tree") },
                modifier = Modifier.testTag("tab_tree")
              )
              Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = { Text("تنظیمات و فیلتر", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Tune, contentDescription = "Settings") },
                modifier = Modifier.testTag("tab_settings")
              )
            }

            AnimatedContent(
              targetState = selectedTabIndex,
              transitionSpec = { fadeIn() togetherWith fadeOut() },
              label = "TabContent"
            ) { targetTab ->
              when (targetTab) {
                0 -> JsonViewer(
                  jsonContent = state.jsonContent,
                  onCopyClicked = { viewModel.copyJsonToClipboard(context) },
                  onDownloadClicked = { saveFileLauncher.launch("data.json") },
                  onShareClicked = { viewModel.shareJson(context) }
                )
                1 -> FileTreeViewer(
                  rootNode = state.rootNode
                )
                2 -> FilterSettingsView(
                  config = filterConfig,
                  onConfigChange = { viewModel.updateFilterConfig(it) },
                  onRescanRequested = { viewModel.startScan() }
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyFolderState(
  onSelectFolderClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(110.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.FolderOpen,
        contentDescription = "Folder",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(56.dp)
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "تبدیل ساختار پوشه به فایل data.json",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "یک پوشه از حافظه گوشی انتخاب کنید تا ساختار درختی تمامی پوشه‌ها و فایل‌های زیرمجموعه اسکن شده و بصورت فایل استاندارد JSON آماده کپی و دانلود گردد.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      lineHeight = 22.sp
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = onSelectFolderClicked,
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
      ),
      modifier = Modifier
        .fillMaxWidth(0.85f)
        .height(52.dp)
        .testTag("empty_state_select_folder_button")
    ) {
      Icon(Icons.Default.FolderOpen, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text("انتخاب فولدر (Select Folder)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Feature highlights list
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      )
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        FeatureItem(
          icon = Icons.Default.AccountTree,
          title = "شبیه‌سازی دقیق ساختار درختی",
          description = "نمایش متناظر فولدرها و فایل‌ها بصورت سلسله‌مراتبی و منظم"
        )
        FeatureItem(
          icon = Icons.Default.FilterAlt,
          title = "فیلتر پسوند فایل‌ها",
          description = "امکان درج یا مستثنی کردن پسوندهای دلخواه (عکس، داکیومنت، کد و...)"
        )
        FeatureItem(
          icon = Icons.Default.Download,
          title = "ذخیره و دانلود به نام data.json",
          description = "ذخیره مستقیم با نام data.json در حافظه و کپی سریع در کلیپ‌بورد"
        )
      }
    }
  }
}

@Composable
private fun FeatureItem(
  icon: ImageVector,
  title: String,
  description: String
) {
  Row(
    verticalAlignment = Alignment.Top,
    modifier = Modifier.fillMaxWidth()
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier
        .size(20.dp)
        .padding(top = 2.dp)
    )
    Spacer(modifier = Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Start
      )
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Start
      )
    }
  }
}

@Composable
private fun ScanningProgressState(
  scanning: UiScanState.Scanning,
  onCancelClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    CircularProgressIndicator(
      modifier = Modifier.size(56.dp),
      strokeWidth = 4.dp,
      color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
      text = "در حال اسکن ساختار پوشه...",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "${scanning.foldersCount} پوشه | ${scanning.filesCount} فایل یافته شد",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.primary,
      fontWeight = FontWeight.SemiBold
    )

    if (scanning.currentPath.isNotEmpty()) {
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = scanning.currentPath,
        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
        color = MaterialTheme.colorScheme.outline,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    LinearProgressIndicator(
      modifier = Modifier
        .fillMaxWidth(0.85f)
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
        .testTag("scan_linear_progress_bar"),
      color = MaterialTheme.colorScheme.primary,
      trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    )

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedButton(
      onClick = onCancelClicked,
      shape = RoundedCornerShape(10.dp)
    ) {
      Text("لغو اسکن (Cancel)")
    }
  }
}

@Composable
private fun ErrorScanState(
  message: String,
  onRetryClicked: () -> Unit,
  onSelectNewFolderClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = Icons.Default.ErrorOutline,
      contentDescription = "Error",
      tint = MaterialTheme.colorScheme.error,
      modifier = Modifier.size(64.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "خطا در اسکن پوشه",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.error
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = message,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Button(
        onClick = onRetryClicked,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Icon(Icons.Default.Refresh, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text("تلاش مجدد (Retry)")
      }

      OutlinedButton(onClick = onSelectNewFolderClicked) {
        Icon(Icons.Default.FolderOpen, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text("پوشه دیگر")
      }
    }
  }
}
