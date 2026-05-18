package com.example.lockit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockit.R
import com.example.lockit.viewmodel.LockItViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDetailsScreen(
    id: Long,
    viewModel: LockItViewModel,
    onBack: () -> Unit
) {
    val passwords by viewModel.passwords.collectAsState()
    val passwordEntry = passwords.find { it.id == id }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    passwordEntry?.let { viewModel.deletePassword(it) }
                    showDeleteDialog = false
                    onBack()
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(passwordEntry?.serviceName ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        passwordEntry?.let { entry ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailItem("Email", entry.email)
                DetailItem("Username", entry.username)
                DetailItem("Password", entry.password)
                DetailItem("Website", entry.website)
                DetailItem("Category", entry.category)
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Not found")
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
