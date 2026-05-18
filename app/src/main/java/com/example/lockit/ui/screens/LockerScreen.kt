package com.example.lockit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockit.model.PasswordEntry
import com.example.lockit.viewmodel.LockItViewModel
import java.util.Locale

@Composable
fun LockerScreen(
    viewModel: LockItViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetails: (Long) -> Unit
) {
    val passwords by viewModel.passwords.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categories = listOf("Vault", "Games", "Social Media", "Work")
    var selectedCategory by remember { mutableStateOf("Social Media") }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "YOUR SECURITY STARTS HERE. WELCOME BACK.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("What you lookin for?") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(categories) { category ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedCategory = category }
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = if (selectedCategory == category) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = category.take(1),
                                        color = if (selectedCategory == category) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Text(text = category, fontSize = 10.sp, fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(passwords.filter { it.category == selectedCategory || selectedCategory == "All" }) { password ->
                        PasswordItem(password = password, onClick = { onNavigateToDetails(password.id) })
                    }
                }
            }

            LargeFloatingActionButton(
                onClick = onNavigateToAdd,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(32.dp),
                shape = MaterialTheme.shapes.extraLarge,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun PasswordItem(password: PasswordEntry, onClick: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = password.serviceName.take(1).uppercase(), fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(text = password.serviceName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = if (isVisible) password.password else "********", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        IconButton(onClick = { isVisible = !isVisible }) {
            Icon(
                imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = null
            )
        }
    }
}
