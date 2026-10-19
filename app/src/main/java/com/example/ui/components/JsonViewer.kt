package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.FormatUtils
import kotlinx.coroutines.delay

@Composable
fun JsonViewer(
  jsonContent: String,
  onCopyClicked: () -> Unit,
  onDownloadClicked: () -> Unit,
  onShareClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var isSearchVisible by remember { mutableStateOf(false) }
  var recentlyCopied by remember { mutableStateOf(false) }

  val lineCount = remember(jsonContent) { jsonContent.lines().size }
  val byteCount = remember(jsonContent) { jsonContent.toByteArray().size.toLong() }

  LaunchedEffect(recentlyCopied) {
    if (recentlyCopied) {
      delay(2000)
      recentlyCopied = false
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("json_viewer_container")
  ) {
    // Action bar with Primary buttons in RTL
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "data.json",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
              Text(
                text = "${lineCount} خط | ${FormatUtils.formatFileSize(byteCount)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          IconButton(
            onClick = {
              isSearchVisible = !isSearchVisible
              if (!isSearchVisible) searchQuery = ""
            },
            modifier = Modifier.testTag("toggle_search_button")
          ) {
            Icon(
              imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
              contentDescription = "Search JSON",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        if (isSearchVisible) {
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("جستجو در متن JSON...", fontSize = 13.sp) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("json_search_input"),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = MaterialTheme.colorScheme.surface,
              focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                val matches = jsonContent.windowed(searchQuery.length) { it == searchQuery }.count { it }
                Text(
                  text = "$matches مورد",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.secondary,
                  modifier = Modifier.padding(end = 8.dp)
                )
              }
            }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Three main action buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = {
              onCopyClicked()
              recentlyCopied = true
            },
            modifier = Modifier
              .weight(1f)
              .testTag("copy_json_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (recentlyCopied) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(
              imageVector = if (recentlyCopied) Icons.Default.Check else Icons.Default.ContentCopy,
              contentDescription = "Copy",
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (recentlyCopied) "کپی شد!" else "کپی (Copy)",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
          }

          FilledTonalButton(
            onClick = onDownloadClicked,
            modifier = Modifier
              .weight(1f)
              .testTag("download_data_json_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Download,
              contentDescription = "Download",
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "ذخیره data.json",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
          }

          OutlinedButton(
            onClick = onShareClicked,
            modifier = Modifier.testTag("share_json_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share",
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }

    // JSON Code container with syntax highlights & search match highlights
    // Wrapped in LTR so JSON indentation and brackets remain visually correct as source code
    Card(
      modifier = Modifier
        .fillMaxSize()
        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      )
    ) {
      val verticalScroll = rememberScrollState()
      val horizontalScroll = rememberScrollState()

      CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .padding(12.dp)
            .verticalScroll(verticalScroll)
            .horizontalScroll(horizontalScroll)
        ) {
          SelectionContainer {
            val highlightedText = remember(jsonContent, searchQuery) {
              if (searchQuery.isBlank()) {
                buildAnnotatedString { append(jsonContent) }
              } else {
                buildAnnotatedString {
                  var currentIndex = 0
                  val query = searchQuery
                  while (currentIndex < jsonContent.length) {
                    val nextIndex = jsonContent.indexOf(query, currentIndex, ignoreCase = true)
                    if (nextIndex >= 0) {
                      append(jsonContent.substring(currentIndex, nextIndex))
                      withStyle(
                        SpanStyle(
                          background = androidx.compose.ui.graphics.Color(0xFFFEF08A),
                          color = androidx.compose.ui.graphics.Color(0xFF854D0E),
                          fontWeight = FontWeight.Bold
                        )
                      ) {
                        append(jsonContent.substring(nextIndex, nextIndex + query.length))
                      }
                      currentIndex = nextIndex + query.length
                    } else {
                      append(jsonContent.substring(currentIndex))
                      break
                    }
                  }
                }
              }
            }

            Text(
              text = highlightedText,
              style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp
              ),
              textAlign = TextAlign.Start,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.testTag("json_raw_code_text")
            )
          }
        }
      }
    }
  }
}
