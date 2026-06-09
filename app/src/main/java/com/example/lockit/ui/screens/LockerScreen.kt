package com.example.lockit.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.lockit.R
import com.example.lockit.model.PasswordEntry
import com.example.lockit.ui.icons.ServiceIcon
import com.example.lockit.viewmodel.LockItViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LockerScreen(
    viewModel: LockItViewModel,
    onNavigateToAdd: () -> Unit
) {
    val passwords by viewModel.passwords.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var selectedCategory by remember { mutableStateOf("") }
    var showAddFolderDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<String?>(null) }
    var editingCount by remember { mutableStateOf(0) }

    // Keep the selection valid as folders load or get added/removed.
    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && selectedCategory !in categories) {
            selectedCategory = if ("Social Media" in categories) "Social Media" else categories.first()
        }
    }

    if (showAddFolderDialog) {
        AddFolderDialog(
            onDismiss = { showAddFolderDialog = false },
            onConfirm = { name ->
                viewModel.addCategory(name)
                selectedCategory = name.trim()
                showAddFolderDialog = false
            }
        )
    }

    editingCategory?.let { cat ->
        EditFolderDialog(
            initialName = cat,
            passwordCount = editingCount,
            onDismiss = { editingCategory = null },
            onSave = { newName ->
                viewModel.renameCategory(cat, newName)
                if (selectedCategory == cat) selectedCategory = newName.trim()
                editingCategory = null
            },
            onDelete = {
                viewModel.deleteCategory(cat)
                // Clearing the selection lets the LaunchedEffect pick a valid folder.
                if (selectedCategory == cat) selectedCategory = ""
                editingCategory = null
            }
        )
    }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.locker_header),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
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
                            modifier = Modifier.combinedClickable(
                                onClick = { selectedCategory = category },
                                onLongClick = {
                                    editingCategory = category
                                    viewModel.getPasswordCount(category) { editingCount = it }
                                }
                            )
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

                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showAddFolderDialog = true }
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CreateNewFolder,
                                        contentDescription = stringResource(R.string.new_folder),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Text(text = stringResource(R.string.new_label), fontSize = 10.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // While searching, show matches from every folder; otherwise scope to the
                // selected folder.
                val visiblePasswords = if (searchQuery.isBlank()) {
                    passwords.filter { it.category == selectedCategory || selectedCategory == "All" }
                } else {
                    passwords
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    items(visiblePasswords) { password ->
                        PasswordItem(
                            password = password,
                            onDelete = { viewModel.deletePassword(password) }
                        )
                    }
                }
            }

            Surface(
                onClick = onNavigateToAdd,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(32.dp)
                    .size(64.dp),
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_folder)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(stringResource(R.string.folder_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun EditFolderDialog(
    initialName: String,
    passwordCount: Int,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var confirmingDelete by remember { mutableStateOf(false) }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text(stringResource(R.string.delete_folder_q)) },
            text = {
                Text(
                    if (passwordCount > 0)
                        stringResource(R.string.delete_folder_msg, initialName, passwordCount)
                    else
                        stringResource(R.string.delete_folder_msg_empty, initialName)
                )
            },
            confirmButton = {
                TextButton(onClick = onDelete) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDelete = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_folder)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(stringResource(R.string.folder_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { confirmingDelete = true }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        }
    )
}

@Composable
fun PasswordItem(password: PasswordEntry, onDelete: () -> Unit) {
    var revealed by remember { mutableStateOf(false) }
    var showDetailsPopup by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    if (showDetailsPopup) {
        PasswordDetailsPopup(
            password = password,
            onDelete = {
                showDetailsPopup = false
                onDelete()
            },
            onDismiss = { showDetailsPopup = false }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Details open only once the password has been revealed.
            .clickable(enabled = revealed) { showDetailsPopup = true },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ServiceIcon(serviceName = password.serviceName, iconKey = password.iconKey)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = password.serviceName,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RevealablePassword(password = password.password, revealed = revealed)
            }

            IconButton(onClick = {
                clipboardManager.setText(AnnotatedString(password.password))
                Toast.makeText(context, context.getString(R.string.password_copied), Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.password_copied),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { revealed = !revealed }) {
                EyeToggleIcon(
                    revealed = revealed,
                    contentDescription = stringResource(if (revealed) R.string.hide_password else R.string.reveal_password)
                )
            }
        }
    }
}

@Composable
private fun EyeToggleIcon(revealed: Boolean, contentDescription: String?) {
    // Animate the strike-through: 0 = no slash (revealed), 1 = full slash (hidden).
    val slash by animateFloatAsState(
        targetValue = if (revealed) 0f else 1f,
        animationSpec = tween(durationMillis = 250),
        label = "eyeSlash"
    )
    val lineColor = LocalContentColor.current

    Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Default.Visibility,
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize()
        )
        if (slash > 0.01f) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val pad = size.minDimension * 0.14f
                val start = Offset(pad, size.height - pad)         // bottom-left
                val fullEnd = Offset(size.width - pad, pad)        // top-right
                val end = Offset(
                    start.x + (fullEnd.x - start.x) * slash,
                    start.y + (fullEnd.y - start.y) * slash
                )
                drawLine(
                    color = lineColor,
                    start = start,
                    end = end,
                    strokeWidth = size.minDimension * 0.09f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

// Pool used to build a fake password placeholder. The decoy is NOT the real password,
// so the actual value is never drawn on screen until the user reveals it.
private const val DECOY_POOL = "wRtPmZqLkXn7bV4aHs9"

private fun decoyFor(password: String): String {
    // Track the real length so the blur looks proportional, but clamp to a single line.
    val n = password.length.coerceIn(6, 14)
    return (0 until n).map { DECOY_POOL[it % DECOY_POOL.length] }.joinToString("")
}

@Composable
private fun RevealablePassword(password: String, revealed: Boolean) {
    val decoy = remember(password) { decoyFor(password) }

    // Modifier.blur only renders on Android 12+ (API 31). On older devices fall back to
    // dots rather than showing the decoy letters un-blurred.
    val canBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    // Crossfade gives a short, smooth fade between blurred decoy and the real value. Both
    // states fill the full width and centre their text, so the value stays put (no left
    // shift) and the tile never resizes while toggling. The real password is only composed
    // while it is actually being revealed.
    Crossfade(
        targetState = revealed,
        animationSpec = tween(durationMillis = 220),
        label = "passwordReveal",
        modifier = Modifier.fillMaxWidth()
    ) { isRevealed ->
        when {
            isRevealed -> Text(
                text = password,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            canBlur -> Text(
                text = decoy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .blur(9.dp, BlurredEdgeTreatment.Unbounded)
            )
            else -> Text(
                text = "•".repeat(decoy.length),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PasswordDetailsPopup(
    password: PasswordEntry,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header: avatar + service name + password, mirroring the list tile.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ServiceIcon(serviceName = password.serviceName, iconKey = password.iconKey)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = password.serviceName,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = password.password, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                PopupField(stringResource(R.string.label_email), password.email)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                PopupField(stringResource(R.string.label_password), password.password)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                PopupField(stringResource(R.string.label_website), password.website)
            }
        }
    }
}

@Composable
private fun PopupField(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
        Text(
            text = value.ifBlank { "—" },
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
