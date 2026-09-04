package com.noobdevs.osint.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.noobdevs.osint.data.PostItem
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
    val searchQuery by viewModel.searchQuery.collectAsState()

    var activeSearchText by remember { mutableStateOf(searchQuery) }

    val provinces = listOf("All", "Balochistan", "KPK", "AJK / Kashmir", "Sindh", "Punjab", "International")
    val attackTypes = listOf("ALL", "AMBUSH_FIRE", "IED_EXPLOSIVE", "CIVIL_UNREST", "SABOTAGE", "GENERAL")

    // Filter posts by province locally
    val filteredPosts = remember(posts, selectedProvince) {
        if (selectedProvince == "All") posts
        else posts.filter { it.detectProvince().equals(selectedProvince, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filters Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = activeSearchText,
                onValueChange = {
                    activeSearchText = it
                    viewModel.setSearchQuery(it)
                },
                placeholder = { Text("Search intel, hashtags, accounts...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (activeSearchText.isNotEmpty()) {
                        IconButton(onClick = {
                            activeSearchText = ""
                            viewModel.setSearchQuery("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Province Filter Chips (ORGANIZED BY PROVINCE)
            Text(
                "PROVINCE / THEATER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(provinces) { province ->
                    FilterChip(
                        selected = selectedProvince == province,
                        onClick = { viewModel.setProvinceFilter(province) },
                        label = { Text(province, fontSize = 12.sp) },
                        leadingIcon = if (selectedProvince == province) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null
                    )
                }
            }

            // Attack Category Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(attackTypes) { attack ->
                    FilterChip(
                        selected = selectedAttackType == attack,
                        onClick = { viewModel.setAttackTypeFilter(attack) },
                        label = { Text(attack.replace("_", " "), fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (attack == "AMBUSH_FIRE" || attack == "IED_EXPLOSIVE") StatusRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        // Posts List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp)
        ) {
            if (error != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Failed to retrieve feed", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            Text(error ?: "", fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadPosts(1, append = false) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }

            items(filteredPosts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    viewModel = viewModel,
                    onOpenVideo = { url ->
                        MediaDownloadHelper.openVideoDownloaderOrTweet(context, url)
                    },
                    onOpenBrowser = { url ->
                        MediaDownloadHelper.openUrlInBrowser(context, url)
                    }
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    } else if (filteredPosts.isNotEmpty()) {
                        OutlinedButton(onClick = { viewModel.loadNextPage() }) {
                            Icon(Icons.Default.ExpandMore, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Load More Intelligence")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: PostItem,
    viewModel: MainViewModel,
    onOpenVideo: (String) -> Unit,
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
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isAttack) StatusRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Meta Header
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
                        Text(
                            post.account ?: "Unknown Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            post.scrapedDate?.take(16)?.replace("T", " ") ?: "Recent",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Province Tag
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = provinceColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        province,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = provinceColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Attack Type & Status Badges
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

            // Post Content
            Text(
                post.content.orEmpty(),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            // Intercepted Image Preview (if present)
            if (mediaUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(10.dp))
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
                        Text(tag, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Tactical Action Row (Download Vid, Download Pic, Open Tweet)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Download/Open Video Option
                    if (!post.url.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = { onOpenVideo(post.url) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PlayCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download Vid", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Download Intercepted Image (if available)
                    if (mediaUrl != null) {
                        FilledTonalButton(
                            onClick = { viewModel.downloadImage(post) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Pic", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (!post.url.isNullOrBlank()) {
                    IconButton(onClick = { onOpenBrowser(post.url) }) {
                        Icon(Icons.Default.OpenInNew, contentDescription = "Open Tweet", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
