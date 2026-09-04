package com.noobdevs.osint.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.noobdevs.osint.data.PipelineFilter
import com.noobdevs.osint.data.PostItem
import com.noobdevs.osint.data.TimeRange
import com.noobdevs.osint.ui.MainViewModel
import com.noobdevs.osint.ui.theme.*
import com.noobdevs.osint.util.MediaDownloadHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isPostsLoading.collectAsState()
    val error by viewModel.postsError.collectAsState()
    val selectedProvince by viewModel.selectedProvince.collectAsState()
    val selectedAttackType by viewModel.selectedAttackType.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    val selectedPipeline by viewModel.selectedPipeline.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var activeSearchText by remember { mutableStateOf(searchQuery) }
    var postForVideoDownloader by remember { mutableStateOf<PostItem?>(null) }

    val provinces = listOf("All", "Balochistan", "KPK", "AJK / Kashmir", "Sindh", "Punjab", "International")
    val attackTypes = listOf("ALL", "AMBUSH_FIRE", "IED_EXPLOSIVE", "CIVIL_UNREST", "SABOTAGE", "GENERAL")

    // Filter posts by province locally
    val filteredPosts = remember(posts, selectedProvince) {
        if (selectedProvince == "All") posts
        else posts.filter { it.detectProvince().equals(selectedProvince, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Comprehensive Tactical Filters Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = activeSearchText,
                onValueChange = {
                    activeSearchText = it
                    viewModel.setSearchQuery(it)
                },
                placeholder = { Text("Search intelligence wire, accounts, keywords...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (activeSearchText.isNotEmpty()) {
                        IconButton(onClick = {
                            activeSearchText = ""
                            viewModel.setSearchQuery("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // 1. TIME RANGE FILTERS (FROM WEBSITE)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("TIME:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(TimeRange.values()) { tr ->
                        FilterChip(
                            selected = selectedTimeRange == tr,
                            onClick = { viewModel.setTimeRange(tr) },
                            label = { Text(tr.label, fontSize = 11.sp) },
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }
            }

            // 2. PIPELINE FILTERS (FROM WEBSITE: Regional News vs Sentiment)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("PIPE:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(PipelineFilter.values()) { pipe ->
                        FilterChip(
                            selected = selectedPipeline == pipe,
                            onClick = { viewModel.setPipelineFilter(pipe) },
                            label = { Text(pipe.label, fontSize = 11.sp) },
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }
            }

            // 3. PROVINCE FILTERS (ORGANIZED BY PROVINCE)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("ZONE:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(provinces) { province ->
                        FilterChip(
                            selected = selectedProvince == province,
                            onClick = { viewModel.setProvinceFilter(province) },
                            label = { Text(province, fontSize = 11.sp) },
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }
            }

            // 4. ATTACK TYPE FILTERS
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("TYPE:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusRed)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(attackTypes) { attack ->
                        FilterChip(
                            selected = selectedAttackType == attack,
                            onClick = { viewModel.setAttackTypeFilter(attack) },
                            label = { Text(attack.replace("_", " "), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (attack == "AMBUSH_FIRE" || attack == "IED_EXPLOSIVE") StatusRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        // Posts Stream
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
        ) {
            if (error != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Failed to retrieve intelligence wire", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            Text(error ?: "", fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(onClick = { viewModel.loadPosts(1, append = false) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }

            items(filteredPosts, key = { it.id }) { post ->
                EnhancedPostCard(
                    post = post,
                    viewModel = viewModel,
                    onOpenVideoDownloader = { postForVideoDownloader = post },
                    onOpenBrowser = { url -> MediaDownloadHelper.openUrlInBrowser(context, url) }
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(26.dp))
                    } else if (filteredPosts.isNotEmpty()) {
                        OutlinedButton(onClick = { viewModel.loadNextPage() }) {
                            Icon(Icons.Default.ExpandMore, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Load More Intelligence (${filteredPosts.size} showing)")
                        }
                    }
                }
            }
        }
    }

    // DEDICATED VIDEO DOWNLOADER & MEDIA GRABBER DIALOG
    postForVideoDownloader?.let { post ->
        val tweetUrl = post.url.orEmpty()
        Dialog(onDismissRequest = { postForVideoDownloader = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.DownloadForOffline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Video & Media Downloader", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { postForVideoDownloader = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        "Download direct video stream, audio, or open with external video grabber",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("SOURCE URL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(tweetUrl, fontSize = 11.sp, fontFamily = FontFamily.Monospace, maxLines = 2)
                        }
                    }

                    // Action 1: Open Video / Tweet
                    Button(
                        onClick = {
                            MediaDownloadHelper.openVideoDownloaderOrTweet(context, tweetUrl)
                            postForVideoDownloader = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayCircleFilled, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Video via Browser / App")
                    }

                    // Action 2: Copy Direct Link
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Tweet Video URL", tweetUrl))
                            Toast.makeText(context, "Copied link to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Video URL")
                    }

                    // Action 3: Save Intercepted Image (if present)
                    if (post.hasMedia) {
                        FilledTonalButton(
                            onClick = {
                                viewModel.downloadImage(post)
                                postForVideoDownloader = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save High-Res Intercepted Picture")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedPostCard(
    post: PostItem,
    viewModel: MainViewModel,
    onOpenVideoDownloader: () -> Unit,
    onOpenBrowser: (String) -> Unit
) {
    val context = LocalContext.current
    val province = post.detectProvince()
    val mediaUrl = viewModel.apiClient.getMediaUrl(post.sentimentImagePath)

    val provinceColor = when (province) {
        "Balochistan" -> StatusAmber
        "KPK" -> StatusPurple
        "AJK / Kashmir" -> MaterialTheme.colorScheme.primary
        "Sindh" -> StatusGreen
        "Punjab" -> Color(0xFF0284C7)
        "International" -> Color(0xFF2563EB)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val isAttack = post.attackType == "AMBUSH_FIRE" || post.attackType == "IED_EXPLOSIVE" || post.attackType == "SABOTAGE"

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isAttack) StatusRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header: Account & Province
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            post.account?.take(1)?.uppercase() ?: "@",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 12.sp
                        )
                    }
                    Column {
                        Text(post.account ?: "Unknown Account", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "${post.sourcePipeline ?: "feed"} • ${post.scrapedDate?.take(16)?.replace("T", " ") ?: "Recent"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(shape = RoundedCornerShape(6.dp), color = provinceColor.copy(alpha = 0.15f)) {
                    Text(
                        province,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = provinceColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Badges Row
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (!post.attackType.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isAttack) StatusRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            post.attackType.replace("_", " "),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAttack) StatusRed else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (post.isShaheedIncident == 1) {
                    Surface(shape = RoundedCornerShape(4.dp), color = StatusRed.copy(alpha = 0.2f)) {
                        Text("INCIDENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusRed, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                if (post.isHighProfile == 1) {
                    Surface(shape = RoundedCornerShape(4.dp), color = StatusAmber.copy(alpha = 0.2f)) {
                        Text("HIGH PROFILE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusAmber, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            // Content
            Text(
                post.content.orEmpty(),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 19.sp
            )

            // Intercepted Image Preview
            if (mediaUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(mediaUrl)
                            .addHeader("Authorization", viewModel.authManager.getBasicAuthHeader())
                            .crossfade(true)
                            .build(),
                        contentDescription = "Intercepted Media",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Hashtags
            if (post.hashtags.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(post.hashtags) { tag ->
                        Text(tag, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Action Row with Video Downloader
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Video Downloader Trigger
                    if (!post.url.isNullOrBlank()) {
                        Button(
                            onClick = onOpenVideoDownloader,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download Vid", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (mediaUrl != null) {
                        FilledTonalButton(
                            onClick = { viewModel.downloadImage(post) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Pic", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (!post.url.isNullOrBlank()) {
                    IconButton(onClick = { onOpenBrowser(post.url) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.OpenInNew, contentDescription = "Open Tweet", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
