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
import com.example.lockit.util.LocaleHelper
import com.example.lockit.viewmodel.LockItViewModel
import java.util.*

@Composable
fun SettingsScreen(
    viewModel: LockItViewModel,
    onNavigateToAccount: () -> Unit,
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
        val context = LocalContext.current
        val currentLang = remember { LocaleHelper.getLanguage(context).ifBlank { "en" } }
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language)) },
            text = {
                Column {
                    LanguageOption(
                        label = "English",
                        selected = currentLang == "en",
                        onClick = {
                            showLanguageDialog = false
                            LocaleHelper.setLanguage(context, "en")
                        }
                    )
                    LanguageOption(
                        label = "Polski",
                        selected = currentLang == "pl",
                        onClick = {
                            showLanguageDialog = false
                            LocaleHelper.setLanguage(context, "pl")
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.manage_lockit), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        
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
        Text(text = user?.username ?: stringResource(R.string.default_username), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            item { SettingItem(stringResource(R.string.account), onNavigateToAccount) }
            item { SettingItem(stringResource(R.string.language), { showLanguageDialog = true }) }
            item { SettingItem(stringResource(R.string.reset_master_key), { showResetDialog = true }) }
            item { SettingItem(stringResource(R.string.app_tutorial), onNavigateToVideo) }
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
private fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
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
