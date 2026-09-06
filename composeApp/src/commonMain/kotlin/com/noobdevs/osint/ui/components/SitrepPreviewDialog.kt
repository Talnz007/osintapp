package com.noobdevs.osint.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.noobdevs.osint.data.models.PostItem
import com.noobdevs.osint.data.models.StatsResponse
import com.noobdevs.osint.platform.PlatformActions
import com.noobdevs.osint.ui.theme.StatusAmber
import com.noobdevs.osint.ui.theme.StatusGreen
import com.noobdevs.osint.ui.theme.StatusRed
import com.noobdevs.osint.ui.theme.WhatsAppGreen
import com.noobdevs.osint.util.SitrepFormatter

@Composable
fun SitrepPreviewDialog(
    posts: List<PostItem>,
    stats: StatsResponse?,
    onDismiss: () -> Unit,
    onShowMessage: (String) -> Unit = {}
) {
    val sitrepText = SitrepFormatter.generate24hStrategicSitrep(posts, stats)
    var selectedViewMode by remember { mutableStateOf(0) } // 0 = Formatted Briefing, 1 = Raw Markdown

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "24h Strategic SITREP",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Executive Intelligence Briefing",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // View Mode Switcher Tab: Formatted vs Raw
                TabRow(
                    selectedTabIndex = selectedViewMode,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    indicator = {},
                    divider = {}
                ) {
                    Tab(
                        selected = selectedViewMode == 0,
                        onClick = { selectedViewMode = 0 },
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Executive View", fontSize = 12.sp, fontWeight = if (selectedViewMode == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                    Tab(
                        selected = selectedViewMode == 1,
                        onClick = { selectedViewMode = 1 },
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Raw Markdown", fontSize = 12.sp, fontWeight = if (selectedViewMode == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                // Content Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (selectedViewMode == 0) {
                            SitrepMarkdownRenderer(markdownText = sitrepText)
                        } else {
                            Text(
                                text = sitrepText,
                                fontSize = 11.5.sp,
                                lineHeight = 17.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Action Buttons: WhatsApp Dispatch & Copy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            PlatformActions.sendWhatsApp(sitrepText)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Send via WhatsApp",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            PlatformActions.copyToClipboard(sitrepText, "OSINT SITREP")
                            onShowMessage("SITREP copied to clipboard")
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Rich Compose Multiplatform Markdown Renderer.
 * Parses headers, bold text (*bold* / **bold**), bullets, dividers, and severity callouts.
 */
@Composable
fun SitrepMarkdownRenderer(
    markdownText: String,
    modifier: Modifier = Modifier
) {
    val lines = markdownText.lines()
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.isBlank() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                // Horizontal divider lines
                trimmed.startsWith("━") || trimmed == "---" || trimmed.startsWith("----") -> {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                // Confidential Header Badge
                trimmed.contains("CONFIDENTIAL // FLASH OSINT", ignoreCase = true) -> {
                    Surface(
                        color = StatusRed.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = StatusRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "CONFIDENTIAL // FLASH OSINT THREAT SITREP",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.5.sp,
                                color = StatusRed
                            )
                        }
                    }
                }
                // Severity Advisory Banner
                trimmed.contains("LEVEL 4", ignoreCase = true) ||
                trimmed.contains("LEVEL 3", ignoreCase = true) ||
                trimmed.contains("LEVEL 2", ignoreCase = true) ||
                trimmed.contains("LEVEL 1", ignoreCase = true) -> {
                    val badgeColor = when {
                        trimmed.contains("LEVEL 4", ignoreCase = true) -> StatusRed
                        trimmed.contains("LEVEL 3", ignoreCase = true) -> StatusAmber
                        else -> StatusGreen
                    }
                    Surface(
                        color = badgeColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = parseMarkdownInline(trimmed, primaryColor, onSurface),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                    }
                }
                // Section Header (e.g., "1. *STRATEGIC SECTOR METRICS*")
                Regex("""^\d+\.\s+\*.+\*""").containsMatchIn(trimmed) || Regex("""^\d+\.\s+[A-Z\s]+""").matches(trimmed) -> {
                    val cleanHeader = trimmed.replace("*", "")
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp, 15.dp)
                                    .background(primaryColor, RoundedCornerShape(2.dp))
                            )
                            Text(
                                text = cleanHeader,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryColor
                            )
                        }
                    }
                }
                // Bullet items
                trimmed.startsWith("•") || trimmed.startsWith("-") -> {
                    val bulletContent = trimmed.removePrefix("•").removePrefix("-").trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            fontWeight = FontWeight.Black,
                            color = primaryColor,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                        Text(
                            text = parseMarkdownInline(bulletContent, primaryColor, onSurface),
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = onSurface
                        )
                    }
                }
                // Salutation / Footer
                trimmed.startsWith("AOA", ignoreCase = true) ||
                trimmed.contains("Transmitted via", ignoreCase = true) ||
                trimmed.equals("Regards", ignoreCase = true) -> {
                    Text(
                        text = parseMarkdownInline(trimmed, primaryColor, onSurfaceVariant),
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        color = onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                else -> {
                    Text(
                        text = parseMarkdownInline(trimmed, primaryColor, onSurface),
                        fontSize = 12.sp,
                        lineHeight = 17.5.sp,
                        color = onSurface
                    )
                }
            }
        }
    }
}

/**
 * Parses inline markdown elements (*bold*, **bold**, _italic_, `code`) into an AnnotatedString.
 */
fun parseMarkdownInline(
    text: String,
    primaryColor: Color,
    onSurfaceColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val pattern = Regex("""(\*\*([^*]+)\*\*|\*([^*]+)\*|_([^_]+)_|`([^`]+)`)""")
        val matches = pattern.findAll(text).toList()

        for (match in matches) {
            if (match.range.first > cursor) {
                append(text.substring(cursor, match.range.first))
            }
            val fullMatch = match.value
            when {
                fullMatch.startsWith("**") && fullMatch.endsWith("**") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = onSurfaceColor)) {
                        append(match.groupValues[2])
                    }
                }
                fullMatch.startsWith("*") && fullMatch.endsWith("*") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = onSurfaceColor)) {
                        append(match.groupValues[3])
                    }
                }
                fullMatch.startsWith("_") && fullMatch.endsWith("_") -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = onSurfaceColor)) {
                        append(match.groupValues[4])
                    }
                }
                fullMatch.startsWith("`") && fullMatch.endsWith("`") -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            background = primaryColor.copy(alpha = 0.12f)
                        )
                    ) {
                        append(" ${match.groupValues[5]} ")
                    }
                }
            }
            cursor = match.range.last + 1
        }
        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}

