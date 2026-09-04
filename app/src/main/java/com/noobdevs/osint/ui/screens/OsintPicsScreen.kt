package com.noobdevs.osint.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.noobdevs.osint.data.PostItem
import com.noobdevs.osint.ui.MainViewModel
import com.noobdevs.osint.ui.theme.*
import com.noobdevs.osint.util.MediaDownloadHelper

@Composable
fun OsintPicsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val posts by viewModel.posts.collectAsState()
    val mediaPosts = remember(posts) {
        posts.filter { it.hasMedia }
    }

    var selectedPostForViewer by remember { mutableStateOf<PostItem?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Section Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("OSINT Intercepted Imagery", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text(
                "High-resolution visual intelligence, intercepted graphics & photo surveillance",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        if (mediaPosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.ImageNotSupported, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("No intercepted pictures loaded yet", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Button(onClick = { viewModel.loadPosts(page = 1, append = false) }) {
                        Text("Fetch Latest Imagery")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(mediaPosts, key = { it.id }) { post ->
                    val mediaUrl = viewModel.apiClient.getMediaUrl(post.sentimentImagePath)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { selectedPostForViewer = post }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                if (mediaUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(mediaUrl)
                                            .addHeader("Authorization", viewModel.authManager.getBasicAuthHeader())
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .align(Alignment.TopEnd)
                                ) {
                                    Text(
                                        post.detectProvince(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    post.account ?: "@Unknown",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    post.content.orEmpty(),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    lineHeight = 14.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.downloadImage(post) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(16.dp))
                                    }
                                    if (!post.url.isNullOrBlank()) {
                                        IconButton(
                                            onClick = { MediaDownloadHelper.openVideoDownloaderOrTweet(context, post.url) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.PlayCircleOutline, contentDescription = "Video", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Full Screen Zoom / Inspector Dialog
    selectedPostForViewer?.let { post ->
        val mediaUrl = viewModel.apiClient.getMediaUrl(post.sentimentImagePath)
        Dialog(onDismissRequest = { selectedPostForViewer = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(post.account ?: "OSINT Image", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { selectedPostForViewer = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    if (mediaUrl != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(mediaUrl)
                                    .addHeader("Authorization", viewModel.authManager.getBasicAuthHeader())
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Text(post.content.orEmpty(), fontSize = 13.sp, lineHeight = 18.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.downloadImage(post)
                                selectedPostForViewer = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Image")
                        }

                        if (!post.url.isNullOrBlank()) {
                            OutlinedButton(
                                onClick = {
                                    MediaDownloadHelper.openVideoDownloaderOrTweet(context, post.url)
                                    selectedPostForViewer = null
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayCircleOutline, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Download Vid")
                            }
                        }
                    }
                }
            }
        }
    }
}
