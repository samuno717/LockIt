package com.example.lockit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun GeneratorScreen(onBack: () -> Unit) {
    var length by remember { mutableStateOf(12f) }
    var includeUppercase by remember { mutableStateOf(true) }
    var includeLowercase by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(false) }
    var minNumbers by remember { mutableStateOf(3) }
    var minSymbols by remember { mutableStateOf(1) }
    
    var generatedPassword by remember { mutableStateOf("") }
    
    fun generate() {
        val chars = (if (includeLowercase) "abcdefghijklmnopqrstuvwxyz" else "") +
                (if (includeUppercase) "ABCDEFGHIJKLMNOPQRSTUVWXYZ" else "") +
                (if (includeNumbers) "0123456789" else "") +
                (if (includeSymbols) "!@#$%^&*()_+" else "")
        if (chars.isEmpty()) {
            generatedPassword = ""
            return
        }
        generatedPassword = (1..length.toInt())
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }

    LaunchedEffect(length, includeUppercase, includeLowercase, includeNumbers, includeSymbols) {
        generate()
    }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Generator", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        
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
                    IconButton(onClick = { /* Copy */ }) {
                        Icon(Icons.Default.ContentCopy, null)
                    }
                }
            },
            shape = MaterialTheme.shapes.medium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Length: ${length.toInt()}", fontSize = 14.sp)
        }
        Slider(
            value = length,
            onValueChange = { length = it },
            valueRange = 8f..32f,
            steps = 23
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        GeneratorToggle("A-Z", includeUppercase) { includeUppercase = it }
        GeneratorToggle("a-z", includeLowercase) { includeLowercase = it }
        GeneratorToggle("0-9", includeNumbers) { includeNumbers = it }
        GeneratorToggle("!@#$%^", includeSymbols) { includeSymbols = it }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CounterRow("Minimum numbers", minNumbers) { minNumbers = it }
        CounterRow("Minimum symbols", minSymbols) { minSymbols = it }
    }
}

@Composable
fun GeneratorToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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
