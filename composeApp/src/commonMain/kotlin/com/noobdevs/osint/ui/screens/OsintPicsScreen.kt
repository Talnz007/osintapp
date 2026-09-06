package com.noobdevs.osint.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.noobdevs.osint.data.models.OsintLexicon
import com.noobdevs.osint.data.models.PostItem
import com.noobdevs.osint.platform.PlatformActions
import com.noobdevs.osint.ui.MainViewModel
import com.noobdevs.osint.ui.components.WhatsAppGreen
import com.noobdevs.osint.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OsintPicsScreen(viewModel: MainViewModel) {
    val posts by viewModel.posts.collectAsState()
    val mediaPosts = remember(posts) { posts.filter { it.hasMedia } }

    var selectedSubTab by remember { mutableStateOf(0) }
    var selectedPostForViewer by remember { mutableStateOf<PostItem?>(null) }
    var showLexiconDialog by remember { mutableStateOf(false) }

    val prominentPosts = remember(posts) {
        posts.filter { it.isHighProfile == 1 || it.hadImage == 1 }.take(3).ifEmpty { posts.take(3) }
    }
    val majorClaims = remember(posts) {
        posts.filter { it.attackType != "GENERAL" && !it.attackType.isNullOrBlank() }.take(4)
            .ifEmpty { posts.filter { it.isShaheedIncident == 1 }.take(4) }
            .ifEmpty { posts.takeLast(3) }
    }

    val periodCoveredText = "Pd Covering – Live 24-Hour Surveillance Window"

    Column(modifier = Modifier.fillMaxSize()) {
        // Flush Executive Subtab Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text("Daily OSINT Picture", fontWeight = FontWeight.Bold, fontSize = 12.5.sp) },
                    icon = { Icon(Icons.Default.DashboardCustomize, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("Visual Imagery (${mediaPosts.size})", fontWeight = FontWeight.Bold, fontSize = 12.5.sp) },
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        if (selectedSubTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("DAILY OSINT PICTURE", fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
                            Text(periodCoveredText, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                item {
                    SectionHeaderTag(title = "PROMINENT SOCIAL MEDIA POSTS", icon = Icons.Default.Campaign, badge = "${prominentPosts.size} Top Leads")
                }
                items(prominentPosts) { post ->
                    ProminentPostSitrepCard(post = post, viewModel = viewModel, onOpenMedia = { selectedPostForViewer = post })
                }

                item {
                    SectionHeaderTag(title = "MAJOR CLAIMS & INCIDENTS", icon = Icons.Default.Shield, badge = "${majorClaims.size} Claims")
                }
                items(majorClaims) { claim ->
                    MajorClaimCard(claim = claim, onOpenBrowser = { url -> PlatformActions.openUrl(url) })
                }

                item {
                    TrendsOutreachComparisonCard(posts = posts)
                }

                item {
                    StrategicAssessmentsCard(posts = posts)
                }
            }
        } else {
            if (mediaPosts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.ImageNotSupported, contentDescription = null, modifier = Modifier.size(44.dp))
                        Text("No intercepted pictures loaded in current filter", fontWeight = FontWeight.SemiBold)
                        Button(onClick = { viewModel.loadPosts(page = 1, append = false) }) {
                            Text("Fetch Imagery")
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
                                            model = mediaUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                        modifier = Modifier.padding(6.dp).align(Alignment.TopEnd)
                                    ) {
                                        Text(post.detectProvince(), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(post.account ?: "@Unknown", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(post.content.orEmpty(), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, lineHeight = 14.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { viewModel.downloadImage(post) }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(16.dp))
                                        }
                                        if (!post.url.isNullOrBlank()) {
                                            IconButton(onClick = {
                                                val cleanUrl = post.url.replace("x.com", "twitter.com")
                                                PlatformActions.openUrl(cleanUrl)
                                            }, modifier = Modifier.size(28.dp)) {
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
    }

    // Full Screen Zoom Dialog
    selectedPostForViewer?.let { post ->
        val mediaUrl = viewModel.apiClient.getMediaUrl(post.sentimentImagePath)
        Dialog(onDismissRequest = { selectedPostForViewer = null }) {
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
                                model = mediaUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Text(post.content.orEmpty(), fontSize = 13.sp, lineHeight = 18.sp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                    val cleanUrl = post.url.replace("x.com", "twitter.com")
                                    PlatformActions.openUrl(cleanUrl)
                                    selectedPostForViewer = null
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayCircleOutline, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Link")
                            }
                        }
                    }
                }
            }
        }
    }

    // TACTICAL LEXICON DIALOG
    if (showLexiconDialog) {
        var lexiconSearch by remember { mutableStateOf("") }
        val filteredLexicon = remember(lexiconSearch) {
            if (lexiconSearch.isBlank()) OsintLexicon.entries
            else OsintLexicon.entries.filter {
                it.code.contains(lexiconSearch, ignoreCase = true) ||
                it.fullName.contains(lexiconSearch, ignoreCase = true) ||
                it.description.contains(lexiconSearch, ignoreCase = true) ||
                it.category.contains(lexiconSearch, ignoreCase = true)
            }
        }

        Dialog(onDismissRequest = { showLexiconDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("OSINT Lexicon & Prompts", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        IconButton(onClick = { showLexiconDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        "Official terminology mapping, military abbreviations & strategic assessment guidelines",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = lexiconSearch,
                        onValueChange = { lexiconSearch = it },
                        placeholder = { Text("Filter terms (e.g. FAK, FAH, Ks, IBO)...", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        trailingIcon = {
                            if (lexiconSearch.isNotEmpty()) {
                                IconButton(onClick = { lexiconSearch = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    )

                    HorizontalDivider()

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (lexiconSearch.isBlank()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("FUSION ASSESSMENT RULE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        Text("Actor + Narrative Projection + Strategic Objective + Secondary Narrative + Desired Strategic Effect", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        Text("Max: 2 FAK, 2 FAH, 1 Other actor. Never exceed 45 words per assessment.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        items(filteredLexicon) { entry ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(entry.code, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                                        Text(entry.category, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(entry.fullName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(entry.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (entry.example.isNotEmpty()) {
                                        Text("Example: \"${entry.example}\"", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showLexiconDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Guide")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionHeaderTag(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, badge: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        }
        Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
            Text(badge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProminentPostSitrepCard(post: PostItem, viewModel: MainViewModel, onOpenMedia: () -> Unit) {
    val mediaUrl = viewModel.apiClient.getMediaUrl(post.sentimentImagePath)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(post.account ?: "@Unknown Source", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Surface(shape = RoundedCornerShape(6.dp), color = StatusAmber.copy(alpha = 0.15f)) {
                    Text(post.detectProvince(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusAmber, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
            Text(post.content.orEmpty(), fontSize = 14.5.sp, lineHeight = 21.sp)
            if (mediaUrl != null) {
                Surface(
                    onClick = onOpenMedia,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                ) {
                    AsyncImage(
                        model = mediaUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.sendPostToWhatsApp(post) },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("WhatsApp", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (!post.url.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            val cleanUrl = post.url.replace("x.com", "twitter.com")
                            PlatformActions.openUrl(cleanUrl)
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Watch Video", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MajorClaimCard(claim: PostItem, onOpenBrowser: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, StatusRed.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusRed))
                    Text(claim.attackType?.replace("_", " ") ?: "SECURITY CLAIM", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = StatusRed)
                }
                Text(claim.detectProvince(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(claim.content.orEmpty(), fontSize = 14.sp, lineHeight = 20.sp, maxLines = 4)
            if (!claim.url.isNullOrBlank()) {
                Text(
                    "Source: ${claim.url}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable { onOpenBrowser(claim.url) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsOutreachComparisonCard(posts: List<PostItem>) {
    val ctCount = posts.count { it.attackType != "GENERAL" && !it.attackType.isNullOrBlank() }
    val genCount = posts.size - ctCount

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("TRENDS OUTREACH COMPARISON", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(8.dp), color = StatusRed.copy(alpha = 0.1f), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Counter-Terrorism (CT)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusRed)
                        Text("$ctCount items", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = StatusRed)
                        Text("Kinetic & Insurgent Claims", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("General Trends", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("$genCount items", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        Text("Civilian & Political Stream", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategicAssessmentsCard(posts: List<PostItem>) {
    val fakMentions = posts.count { it.detectProvince() == "KPK" }
    val fahMentions = posts.count { it.detectProvince() == "Balochistan" }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text("STRATEGIC INTEL ASSESSMENTS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("• FAK (Fitna al-Khawarij / Northern Sector)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = StatusPurple)
                    Text(
                        "FAK proj exaggerated claims to demo kinetic ascendancy ($fakMentions indicators) while attempting to project terror tactics against civilians to claim social space along border districts.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("• FAH (Fitna al-Haram / Southern Sector)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = StatusAmber)
                    Text(
                        "FAH propagated exaggerated successes ($fahMentions indicators) to maintain kinetic relevance while alleging state repression to legitimize external resistance campaigns.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
