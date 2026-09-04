package com.noobdevs.osint.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noobdevs.osint.data.AuthManager
import com.noobdevs.osint.ui.MainViewModel
import com.noobdevs.osint.ui.theme.StatusGreen
import com.noobdevs.osint.ui.theme.StatusRed

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var serverUrl by remember { mutableStateOf(viewModel.authManager.baseUrl) }
    var username by remember { mutableStateOf(viewModel.authManager.username) }
    var password by remember { mutableStateOf(viewModel.authManager.password) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var isTestingConnection by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Settings Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Settings & Configuration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Interface personalization & secure office server connection", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // 1. APPEARANCE (Light Mode default + Dark Mode Switch)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Theme & Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dark Mode", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(
                            if (isDarkMode) "Tactical Dark theme enabled" else "Executive Light Mode (Corporate Default)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
            }
        }

        // 2. SERVER CONNECTION & AUTHENTICATION
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Dns, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Office Server Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text(
                    "Cloudflare tunnel URLs change dynamically. Update the host endpoint here without recompiling the application.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Server Base URL") },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("API Username") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("API Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                statusMessage?.let { (success, msg) ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = (if (success) StatusGreen else StatusRed).copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (success) StatusGreen else StatusRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(msg, fontSize = 12.sp, color = if (success) StatusGreen else StatusRed, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Button(
                    onClick = {
                        isTestingConnection = true
                        statusMessage = null
                        viewModel.updateCredentials(serverUrl, username, password) { success, msg ->
                            isTestingConnection = false
                            statusMessage = Pair(success, msg)
                            if (success) {
                                Toast.makeText(context, "Credentials saved & synced!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isTestingConnection,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isTestingConnection) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifying Endpoint...")
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test & Save Connection")
                    }
                }

                TextButton(
                    onClick = {
                        serverUrl = AuthManager.DEFAULT_BASE_URL
                        username = AuthManager.DEFAULT_USERNAME
                        password = AuthManager.DEFAULT_PASSWORD
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Reset to Default Office Endpoint", fontSize = 12.sp)
                }
            }
        }

        // 3. ABOUT & SECURITY
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("About OSINT Command Center", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Version 1.0.0 (Corporate Build)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Real-time threat monitoring, sentiment extraction & multi-province incident reporting.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
