package com.example.lockit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                Text(text = stringResource(R.string.username), fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                Text(text = user?.username ?: stringResource(R.string.not_available), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Column {
                Text(text = stringResource(R.string.id_label), fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                Text(text = user?.id?.toString() ?: stringResource(R.string.not_available), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            Column {
                Text(text = stringResource(R.string.passkey), fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                Text(text = "••••••••", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(R.string.passkey_hidden),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Column {
                Text(text = stringResource(R.string.statistics), fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                Text(
                    text = stringResource(R.string.created_at, user?.createdAt?.let { dateFormatter.format(Date(it)) } ?: stringResource(R.string.not_available)),
                    fontSize = 16.sp
                )
                Text(
                    text = stringResource(R.string.last_login, user?.lastLogin?.let { dateFormatter.format(Date(it)) } ?: stringResource(R.string.not_available)),
                    fontSize = 16.sp
                )
            }

            Column {
                Text(text = stringResource(R.string.info), fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                Text(text = stringResource(R.string.account_info_msg), fontSize = 16.sp)
            }
        }
    }
}
