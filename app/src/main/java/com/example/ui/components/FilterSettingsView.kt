package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FilterMode
import com.example.model.IndentMode
import com.example.model.ScanFilterConfig

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterSettingsView(
  config: ScanFilterConfig,
  onConfigChange: (ScanFilterConfig) -> Unit,
  onRescanRequested: () -> Unit,
  modifier: Modifier = Modifier
) {
  var newExtInput by remember { mutableStateOf("") }
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp)
      .testTag("filter_settings_container"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Section 1: File Extension Filter Mode
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      )
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.FilterAlt,
            contentDescription = "Filter",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "فیلتر پسوند فایل‌ها (Extension Filter)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
          val modes = listOf(
            FilterMode.ALL_FILES to "همه فایل‌ها",
            FilterMode.ONLY_SPECIFIED to "فقط مجازها",
            FilterMode.EXCLUDE_SPECIFIED to "مستثنی کردن"
          )
          modes.forEachIndexed { index, (mode, label) ->
            SegmentedButton(
              selected = config.filterMode == mode,
              onClick = { onConfigChange(config.copy(filterMode = mode)) },
              shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
              modifier = Modifier.testTag("filter_mode_button_$index")
            ) {
              Text(text = label, style = MaterialTheme.typography.labelMedium)
            }
          }
        }

        if (config.filterMode != FilterMode.ALL_FILES) {
          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = if (config.filterMode == FilterMode.ONLY_SPECIFIED)
              "فقط فایل‌هایی با پسوندهای زیر در فایل data.json درج می‌شوند:"
            else
              "فایل‌هایی با پسوندهای زیر از خروجی حذف خواهند شد:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Presets row
          Text(
            text = "افزودن دسته‌جمعی بر اساس گروه:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            val presets = listOf(
              "تصاویر 🖼️" to listOf("jpg", "jpeg", "png", "webp", "gif", "svg"),
              "اسناد 📄" to listOf("pdf", "docx", "txt", "md", "xlsx"),
              "کد و دیتا 💻" to listOf("kt", "java", "py", "js", "ts", "json", "xml", "html"),
              "صوتی/تصویری 🎵" to listOf("mp3", "wav", "mp4", "mkv"),
              "فشرده 📦" to listOf("zip", "rar", "7z", "tar", "gz")
            )
            presets.forEach { (label, exts) ->
              SuggestionChip(
                onClick = {
                  val updated = config.extensions + exts
                  onConfigChange(config.copy(extensions = updated))
                },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Add custom extension input
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = newExtInput,
              onValueChange = { newExtInput = it },
              placeholder = { Text("مثال: pdf یا json", fontSize = 13.sp) },
              singleLine = true,
              modifier = Modifier
                .weight(1f)
                .testTag("new_extension_input"),
              shape = RoundedCornerShape(10.dp)
            )

            Button(
              onClick = {
                val clean = newExtInput.trim().lowercase().removePrefix(".")
                if (clean.isNotEmpty()) {
                  val updated = config.extensions + clean
                  onConfigChange(config.copy(extensions = updated))
                  newExtInput = ""
                }
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("add_extension_button")
            ) {
              Icon(Icons.Default.Add, contentDescription = "Add")
              Spacer(modifier = Modifier.width(4.dp))
              Text("افزودن")
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Extension chips flow layout
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            config.extensions.sorted().forEach { ext ->
              InputChip(
                selected = true,
                onClick = {
                  val updated = config.extensions - ext
                  onConfigChange(config.copy(extensions = updated))
                },
                label = { Text(".$ext", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                trailingIcon = {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove $ext",
                    modifier = Modifier.size(16.dp)
                  )
                },
                colors = InputChipDefaults.inputChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("extension_chip_$ext")
              )
            }
          }
        }
      }
    }

    // Section 2: Scanning & Inclusion Options
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      )
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Options",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "تنظیمات اسکن و ساختار (Scan Options)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingSwitchRow(
          title = "فایل‌ها و فولدرهای مخفی (.*)",
          subtitle = "شامل مواردی نظیر git. یا فایلهای سیستمی",
          checked = config.includeHidden,
          onCheckedChange = { onConfigChange(config.copy(includeHidden = it)) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        SettingSwitchRow(
          title = "شامل فولدرهای خالی",
          subtitle = "پوشه‌هایی که فایلی ندارند در ساختار آورده شوند",
          checked = config.includeEmptyFolders,
          onCheckedChange = { onConfigChange(config.copy(includeEmptyFolders = it)) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        SettingSwitchRow(
          title = "اطلاعات تکمیلی متا‌دیتا",
          subtitle = "حجم فایل، زمان آخرین ویرایش و فرمت MIME",
          checked = config.includeMetadata,
          onCheckedChange = { onConfigChange(config.copy(includeMetadata = it)) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        SettingSwitchRow(
          title = "درج مسیر نسبی (Relative Path)",
          subtitle = "درج ویژگی path برای هر پوشه و فایل",
          checked = config.includeRelativePath,
          onCheckedChange = { onConfigChange(config.copy(includeRelativePath = it)) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Max depth slider
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "حداکثر عمق اسکن (Depth)",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = if (config.maxDepth == 0) "نامحدود (تمام زیرپوشه‌ها)" else "${config.maxDepth} لایه",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary
            )
          }
          Slider(
            value = config.maxDepth.toFloat(),
            onValueChange = { onConfigChange(config.copy(maxDepth = it.toInt())) },
            valueRange = 0f..10f,
            steps = 9,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // Section 3: JSON Format & Indentation
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      )
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.FormatAlignLeft,
              contentDescription = "Format",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "قالب خروجی JSON (Output Format)",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          // Visual badge for Minified vs Pretty-printed
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (config.isMinified) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
          ) {
            Text(
              text = if (config.isMinified) "فشرده (Minified)" else "مرتب (Pretty-printed)",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = if (config.isMinified) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Toggle Switch to choose between Minified or Pretty-printed JSON output
        SettingSwitchRow(
          title = "خروجی فشرده (Minified JSON)",
          subtitle = if (config.isMinified)
            "فعال: خروجی یک‌خطی فشرده بدون فاصله اضافی (Minified)"
          else
            "غیرفعال: ساختار خوانا و مرتب (Pretty-printed)",
          checked = config.isMinified,
          onCheckedChange = { minified ->
            onConfigChange(
              config.copy(
                isMinified = minified,
                indentMode = if (minified) IndentMode.COMPACT else if (config.indentMode == IndentMode.COMPACT) IndentMode.TWO_SPACES else config.indentMode
              )
            )
          },
          modifier = Modifier.testTag("toggle_minified_switch")
        )

        if (!config.isMinified) {
          HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

          Text(
            text = "فاصله‌گذاری فرمت مرتب (Indentation Spaces):",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val prettyModes = listOf(IndentMode.TWO_SPACES, IndentMode.FOUR_SPACES)
            prettyModes.forEach { mode ->
              FilterChip(
                selected = config.indentMode == mode,
                onClick = { onConfigChange(config.copy(indentMode = mode, isMinified = false)) },
                label = { Text(mode.label, fontSize = 12.sp) },
                modifier = Modifier
                  .weight(1f)
                  .testTag("indent_chip_${mode.name}")
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SettingSwitchRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange
    )
  }
}
