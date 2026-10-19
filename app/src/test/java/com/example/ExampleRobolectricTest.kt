package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.FileNode
import com.example.model.FilterMode
import com.example.model.IndentMode
import com.example.model.ScanFilterConfig
import com.example.util.FormatUtils
import com.example.util.JsonTreeBuilder
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Folder2JSON", appName)
  }

  @Test
  fun `formatFileSize handles zero and sizes correctly`() {
    assertEquals("0 B", FormatUtils.formatFileSize(0))
    assertEquals("100 B", FormatUtils.formatFileSize(100))
    assertEquals("1.00 KB", FormatUtils.formatFileSize(1024))
    assertEquals("1.50 MB", FormatUtils.formatFileSize((1024 * 1024 * 1.5).toLong()))
  }

  @Test
  fun `scanFilterConfig filtering logic`() {
    val whitelistConfig = ScanFilterConfig(
      filterMode = FilterMode.ONLY_SPECIFIED,
      extensions = setOf("pdf", "json")
    )
    assertTrue(whitelistConfig.shouldIncludeFile("test.pdf", "pdf"))
    assertTrue(whitelistConfig.shouldIncludeFile("test.JSON", "JSON"))
    assertFalse(whitelistConfig.shouldIncludeFile("test.jpg", "jpg"))

    val blacklistConfig = ScanFilterConfig(
      filterMode = FilterMode.EXCLUDE_SPECIFIED,
      extensions = setOf("tmp", "log")
    )
    assertFalse(blacklistConfig.shouldIncludeFile("error.log", "log"))
    assertTrue(blacklistConfig.shouldIncludeFile("data.json", "json"))
  }

  @Test
  fun `jsonTreeBuilder creates accurate tree structure`() {
    val childFile = FileNode(
      name = "document.txt",
      isDirectory = false,
      path = "/root/document.txt",
      extension = "txt",
      mimeType = "text/plain",
      sizeBytes = 120,
      lastModified = 1700000000000L
    )

    val root = FileNode(
      name = "root",
      isDirectory = true,
      path = "/root",
      children = listOf(childFile)
    )

    val jsonString = JsonTreeBuilder.buildJson(
      rootNode = root,
      config = ScanFilterConfig(indentMode = IndentMode.TWO_SPACES)
    )

    val jsonObject = JSONObject(jsonString)
    assertEquals("root", jsonObject.getString("name"))
    assertEquals("directory", jsonObject.getString("type"))
    val children = jsonObject.getJSONArray("children")
    assertEquals(1, children.length())
    val firstChild = children.getJSONObject(0)
    assertEquals("document.txt", firstChild.getString("name"))
    assertEquals("file", firstChild.getString("type"))
    assertEquals("txt", firstChild.getString("extension"))
  }

  @Test
  fun `jsonTreeBuilder handles minified toggle correctly`() {
    val childFile = FileNode(
      name = "test.txt",
      isDirectory = false,
      path = "/root/test.txt",
      sizeBytes = 10
    )
    val root = FileNode(
      name = "root",
      isDirectory = true,
      path = "/root",
      children = listOf(childFile)
    )

    // Pretty printed
    val prettyJson = JsonTreeBuilder.buildJson(
      rootNode = root,
      config = ScanFilterConfig(isMinified = false, indentMode = IndentMode.TWO_SPACES)
    )
    assertTrue("Pretty printed JSON should contain newlines", prettyJson.contains("\n"))

    // Minified
    val minifiedJson = JsonTreeBuilder.buildJson(
      rootNode = root,
      config = ScanFilterConfig(isMinified = true)
    )
    assertFalse("Minified JSON should not contain newlines", minifiedJson.contains("\n"))
    assertTrue("Minified JSON should be valid", minifiedJson.startsWith("{") && minifiedJson.endsWith("}"))
  }
}
