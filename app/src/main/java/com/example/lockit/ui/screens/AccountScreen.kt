package com.example.lockit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockit.R
import com.example.lockit.viewmodel.LockItViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: LockItViewModel,
    onBack: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    var passkeyVisible by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column {
                Text(text = "Username", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                Text(text = user?.username ?: "N/A", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Column {
                Text(text = "ID", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                Text(text = user?.id?.toString() ?: "N/A", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            Column {
                Text(text = stringResource(R.string.passkey), fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (passkeyVisible) (user?.passkey ?: "N/A") else "****",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { passkeyVisible = !passkeyVisible }) {
                        Icon(
                            imageVector = if (passkeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                }
            }
            
            Column {
                Text(text = "Statistics", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                Text(
                    text = stringResource(R.string.created_at, user?.createdAt?.let { dateFormatter.format(Date(it)) } ?: "N/A"),
                    fontSize = 16.sp
                )
                Text(
                    text = stringResource(R.string.last_login, user?.lastLogin?.let { dateFormatter.format(Date(it)) } ?: "N/A"),
                    fontSize = 16.sp
                )
            }

            Column {
                Text(text = "Info", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                Text(text = "Your account is stored locally and protected by the master passkey.", fontSize = 16.sp)
            }
        }
    }
}
