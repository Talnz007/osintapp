package com.noobdevs.osint.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noobdevs.osint.data.DeckItem
import com.noobdevs.osint.data.PostItem
import com.noobdevs.osint.ui.MainViewModel
import com.noobdevs.osint.ui.UiState
import com.noobdevs.osint.ui.theme.StatusGreen
import com.noobdevs.osint.ui.theme.StatusPurple

@Composable
fun DorScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val decksState by viewModel.decksState.collectAsState()
    val posts by viewModel.posts.collectAsState()

    val dorBulletins = remember(posts) {
        posts.filter { it.isDorReport }
    }

    var selectedSubTab by remember { mutableStateOf(0) } // 0: Briefing Decks (PPTX), 1: Dispatches (Bulletins)

    Column(modifier = Modifier.fillMaxSize()) {
        // DOR Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Daily Operational Reports (DOR)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text(
                "Executive briefing decks & automated operational intelligence bulletins",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryTabRow(selectedTabIndex = selectedSubTab) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text("Briefing Decks (.pptx)", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Slideshow, contentDescription = null) }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("DOR Bulletins (${dorBulletins.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.MarkChatRead, contentDescription = null) }
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        if (selectedSubTab == 0) {
            // Decks List
            when (val state = decksState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Failed to load briefing decks", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            Text(state.message, fontSize = 12.sp)
                            Button(onClick = { viewModel.loadDecks() }) { Text("Retry") }
                        }
                    }
                }
                is UiState.Success -> {
                    val decks = state.data
                    if (decks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No generated briefing decks available")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("GENERATED POWERPOINT BRIEFS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    IconButton(onClick = { viewModel.loadDecks() }) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            items(decks) { deck ->
                                DeckCard(deck = deck, onDownload = { viewModel.downloadDeck(deck) })
                            }
                        }
                    }
                }
                UiState.Idle -> {}
            }
        } else {
            // DOR WhatsApp Bulletins
            if (dorBulletins.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(40.dp))
                        Text("No DOR bulletins in current cached feed", fontWeight = FontWeight.SemiBold)
                        Button(onClick = { viewModel.loadPosts(page = 1, append = false) }) {
                            Text("Refresh Feed")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(dorBulletins, key = { it.id }) { post ->
                        DorBulletinCard(post = post, onCopy = { text ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("DOR Bulletin", text))
                            Toast.makeText(context, "Copied bulletin to clipboard", Toast.LENGTH_SHORT).show()
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun DeckCard(deck: DeckItem, onDownload: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StatusPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Slideshow, contentDescription = null, tint = StatusPurple)
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(deck.filename, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(deck.sizeFormatted, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(deck.modifiedAt, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Button(
                onClick = onDownload,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Download", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun DorBulletinCard(post: PostItem, onCopy: (String) -> Unit) {
    val rawText = post.whatsappMessageSent.orEmpty()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StatusGreen.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(StatusGreen))
                    Text("AUTOMATED INTELLIGENCE DISPATCH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusGreen)
                }
                IconButton(onClick = { onCopy(rawText) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    rawText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Source: ${post.account ?: "Regional News"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(post.scrapedDate?.take(16)?.replace("T", " ") ?: "", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
