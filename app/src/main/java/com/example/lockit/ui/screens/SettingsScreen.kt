package com.example.lockit.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lockit.R
import com.example.lockit.viewmodel.LockItViewModel
import java.util.*

@Composable
fun SettingsScreen(
    viewModel: LockItViewModel,
    onNavigateToAccount: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToVideo: () -> Unit,
    onNavigateToAudio: () -> Unit,
    onLogout: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateProfileImage(it.toString()) }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout_confirm_title)) },
            text = { Text(stringResource(R.string.logout_confirm_msg)) },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_confirm_title)) },
            text = { Text(stringResource(R.string.reset_confirm_msg)) },
            confirmButton = {
                TextButton(onClick = { 
                    showResetDialog = false
                    viewModel.resetMasterKey()
                    onLogout()
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language)) },
            text = {
                Column {
                    TextButton(onClick = { 
                        showLanguageDialog = false 
                    }) { Text("English") }
                    TextButton(onClick = { 
                        showLanguageDialog = false 
                    }) { Text("Polski") }
                }
            },
            confirmButton = {}
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Manage your LOCKIT.", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .clickable { imageLauncher.launch("image/*") },
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                if (user?.profileImage != null) {
                    AsyncImage(
                        model = user?.profileImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                    }
                }
            }
            Surface(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.padding(4.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = user?.username ?: "Linux Enjoyer", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            item { SettingItem(stringResource(R.string.account), onNavigateToAccount) }
            item { SettingItem(stringResource(R.string.notifications), onNavigateToNotifications) }
            item { SettingItem(stringResource(R.string.language), { showLanguageDialog = true }) }
            item { SettingItem(stringResource(R.string.reset_master_key), { showResetDialog = true }) }
            item { SettingItem("App Tutorial", onNavigateToVideo) }
            item { 
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleDarkMode(!isDarkMode) }.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.dark_mode), fontSize = 14.sp)
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            item { SettingItem(stringResource(R.string.logout), { showLogoutDialog = true }) }
        }
    }
}

@Composable
fun SettingItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 14.sp)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
    }
}
