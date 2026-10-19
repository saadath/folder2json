package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileNode
import com.example.util.FormatUtils

@Composable
fun FileTreeViewer(
  rootNode: FileNode,
  modifier: Modifier = Modifier
) {
  // Map of path to expanded boolean state
  val expandedState = remember(rootNode) {
    mutableStateMapOf<String, Boolean>().apply {
      put(rootNode.path, true) // Root is expanded by default
    }
  }

  fun expandAllNodes(node: FileNode) {
    if (node.isDirectory) {
      expandedState[node.path] = true
      node.children?.forEach { expandAllNodes(it) }
    }
  }

  fun collapseAllNodes(node: FileNode) {
    if (node.isDirectory) {
      if (node.path != rootNode.path) {
        expandedState[node.path] = false
      }
      node.children?.forEach { collapseAllNodes(it) }
    }
  }

  val listState = rememberLazyListState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("file_tree_viewer")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "ساختار سلسله‌مراتبی پوشه",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(
          onClick = { expandAllNodes(rootNode) },
          label = { Text("گسترش همه", style = MaterialTheme.typography.labelSmall) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.UnfoldMore,
              contentDescription = "Expand All",
              modifier = Modifier.size(16.dp)
            )
          },
          modifier = Modifier.testTag("expand_all_button")
        )

        AssistChip(
          onClick = { collapseAllNodes(rootNode) },
          label = { Text("بستن همه", style = MaterialTheme.typography.labelSmall) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.UnfoldLess,
              contentDescription = "Collapse All",
              modifier = Modifier.size(16.dp)
            )
          },
          modifier = Modifier.testTag("collapse_all_button")
        )
      }
    }

    Card(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 4.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      )
    ) {
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxSize()
          .padding(8.dp)
      ) {
        item {
          TreeNodeItem(
            node = rootNode,
            depth = 0,
            expandedState = expandedState,
            onToggle = { path ->
              val current = expandedState[path] ?: false
              expandedState[path] = !current
            }
          )
        }
      }
    }
  }
}

@Composable
private fun TreeNodeItem(
  node: FileNode,
  depth: Int,
  expandedState: Map<String, Boolean>,
  onToggle: (String) -> Unit
) {
  val isExpanded = expandedState[node.path] ?: false

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .clickable(enabled = node.isDirectory) {
          if (node.isDirectory) {
            onToggle(node.path)
          }
        }
        .padding(
          start = (depth * 18).dp,
          top = 6.dp,
          bottom = 6.dp,
          end = 8.dp
        ),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (node.isDirectory) {
        Icon(
          imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
          contentDescription = if (isExpanded) "Collapse" else "Expand",
          modifier = Modifier.size(20.dp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
          imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
          contentDescription = "Folder",
          tint = Color(0xFFF59E0B),
          modifier = Modifier.size(22.dp)
        )
      } else {
        Spacer(modifier = Modifier.width(24.dp))
        val (icon, tint) = getFileIconAndColor(node.extension)
        Icon(
          imageVector = icon,
          contentDescription = "File",
          tint = tint,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Text(
        text = node.name,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontFamily = FontFamily.Monospace,
          fontSize = 13.sp,
          fontWeight = if (node.isDirectory) FontWeight.SemiBold else FontWeight.Normal
        ),
        textAlign = TextAlign.Start,
        color = if (node.isDirectory) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f)
      )

      Spacer(modifier = Modifier.width(8.dp))

      if (node.isDirectory) {
        val countText = "${node.childFoldersCount} 📁 ${node.childFilesCount} 📄"
        Box(
          modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = countText,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        Text(
          text = FormatUtils.formatFileSize(node.sizeBytes),
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
          color = MaterialTheme.colorScheme.outline
        )
      }
    }

    if (node.isDirectory && node.children != null) {
      AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          node.children.forEach { child ->
            TreeNodeItem(
              node = child,
              depth = depth + 1,
              expandedState = expandedState,
              onToggle = onToggle
            )
          }
        }
      }
    }
  }
}

@Composable
private fun getFileIconAndColor(extension: String): Pair<ImageVector, Color> {
  return when (FormatUtils.getFileCategory(extension)) {
    FormatUtils.FileCategory.CODE -> Pair(Icons.Default.Code, Color(0xFF3B82F6))
    FormatUtils.FileCategory.IMAGE -> Pair(Icons.Default.Image, Color(0xFF10B981))
    FormatUtils.FileCategory.AUDIO -> Pair(Icons.Default.AudioFile, Color(0xFFEC4899))
    FormatUtils.FileCategory.VIDEO -> Pair(Icons.Default.VideoFile, Color(0xFF8B5CF6))
    FormatUtils.FileCategory.DOCUMENT -> Pair(Icons.Default.Description, Color(0xFFF97316))
    FormatUtils.FileCategory.ARCHIVE -> Pair(Icons.Default.Archive, Color(0xFFEF4444))
    FormatUtils.FileCategory.DATA -> Pair(Icons.Default.DataObject, Color(0xFF06B6D4))
    else -> Pair(Icons.AutoMirrored.Filled.InsertDriveFile, MaterialTheme.colorScheme.onSurfaceVariant)
  }
}
