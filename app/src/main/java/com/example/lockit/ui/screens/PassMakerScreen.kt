package com.example.lockit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockit.R
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassMakerScreen(onBack: () -> Unit) {
    var length by remember { mutableStateOf(12f) }
    var includeUppercase by remember { mutableStateOf(true) }
    var includeLowercase by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(false) }
    var minNumbers by remember { mutableStateOf(3) }
    var minSymbols by remember { mutableStateOf(1) }

    var generatedPassword by remember { mutableStateOf("") }

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copiedMessage = stringResource(R.string.password_copied)

    fun generate() {
        val len = length.toInt()
        val lower = "abcdefghijklmnopqrstuvwxyz"
        val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val digits = "0123456789"
        val symbols = "!@#$%^&*()_+"

        val pools = buildList {
            if (includeLowercase) add(lower)
            if (includeUppercase) add(upper)
            if (includeNumbers) add(digits)
            if (includeSymbols) add(symbols)
        }
        if (pools.isEmpty()) {
            generatedPassword = ""
            return
        }
        val allChars = pools.joinToString("")

        // Reserve the required minimum of each enabled class first, so the result is
        // guaranteed to contain them even when the length is tight.
        val required = mutableListOf<Char>()
        if (includeNumbers) {
            val n = minNumbers.coerceAtMost(len - required.size).coerceAtLeast(0)
            repeat(n) { required.add(digits[Random.nextInt(digits.length)]) }
        }
        if (includeSymbols) {
            val n = minSymbols.coerceAtMost(len - required.size).coerceAtLeast(0)
            repeat(n) { required.add(symbols[Random.nextInt(symbols.length)]) }
        }

        // Fill the remaining slots from the full pool, then shuffle so the required
        // characters are not stuck at the front.
        val remaining = (len - required.size).coerceAtLeast(0)
        val rest = (1..remaining).map { allChars[Random.nextInt(allChars.length)] }
        generatedPassword = (required + rest).shuffled().joinToString("")
    }

    LaunchedEffect(length, includeUppercase, includeLowercase, includeNumbers, includeSymbols, minNumbers, minSymbols) {
        generate()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PassMaker") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = generatedPassword,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    Row {
                        IconButton(onClick = { generate() }) {
                            Icon(Icons.Default.Refresh, "Regenerate")
                        }
                        IconButton(
                            onClick = {
                                if (generatedPassword.isNotEmpty()) {
                                    clipboardManager.setText(AnnotatedString(generatedPassword))
                                    scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, "Copy")
                        }
                    }
                },
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.length_label, length.toInt()), fontSize = 14.sp)
            }
            Slider(
                value = length,
                onValueChange = { length = it },
                valueRange = 8f..32f,
                steps = 23
            )

            Spacer(modifier = Modifier.height(16.dp))

            PassMakerToggle("A-Z", includeUppercase) { includeUppercase = it }
            PassMakerToggle("a-z", includeLowercase) { includeLowercase = it }
            PassMakerToggle("0-9", includeNumbers) { includeNumbers = it }
            PassMakerToggle("!@#$%^", includeSymbols) { includeSymbols = it }

            Spacer(modifier = Modifier.height(16.dp))

            CounterRow(stringResource(R.string.min_numbers), minNumbers) { minNumbers = it.coerceIn(0, length.toInt()) }
            CounterRow(stringResource(R.string.min_symbols), minSymbols) { minSymbols = it.coerceIn(0, length.toInt()) }
        }
    }
}

@Composable
fun PassMakerToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun CounterRow(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (value > 0) onValueChange(value - 1) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Remove, null)
            }
            Text(text = value.toString(), modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(onClick = { onValueChange(value + 1) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Add, null)
            }
        }
    }
}
