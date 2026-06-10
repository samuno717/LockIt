package com.example.lockit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockit.R
import com.example.lockit.model.PasswordEntry
import com.example.lockit.ui.icons.IconPicker
import com.example.lockit.viewmodel.LockItViewModel
import java.util.*

@Composable
fun AddPasswordScreen(
    viewModel: LockItViewModel,
    onBack: () -> Unit
) {
    var website by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val categories by viewModel.categories.collectAsState()
    var selectedCategory by remember { mutableStateOf("") }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var selectedIconKey by remember { mutableStateOf("") }

    LaunchedEffect(categories) {
        if (selectedCategory.isEmpty() && categories.isNotEmpty()) {
            selectedCategory = if ("Social Media" in categories) "Social Media" else categories.first()
        }
    }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = stringResource(R.string.new_login), fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = website,
                        onValueChange = { website = it },
                        placeholder = { Text(stringResource(R.string.field_website)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text(stringResource(R.string.field_email)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text(stringResource(R.string.field_password)) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                            }
                        }
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text(stringResource(R.string.field_folder)) },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { categoryMenuExpanded = true }
                        )
                        DropdownMenu(
                            expanded = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        categoryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.icon),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        IconPicker(
                            selectedKey = selectedIconKey,
                            onSelected = { selectedIconKey = it }
                        )
                    }

                    Button(
                        onClick = {
                            val service = website.substringBefore(".").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                            viewModel.addPassword(
                                PasswordEntry(
                                    serviceName = if (service.isEmpty()) website else service,
                                    email = email,
                                    username = email.substringBefore("@"),
                                    password = password,
                                    website = website,
                                    category = selectedCategory.ifEmpty { "Social Media" },
                                    iconKey = selectedIconKey
                                )
                            )
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(stringResource(R.string.save), color = MaterialTheme.colorScheme.surface)
                    }
                }
            }
        }
    }
}
