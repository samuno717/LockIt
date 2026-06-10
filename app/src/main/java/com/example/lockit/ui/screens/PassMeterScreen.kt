package com.example.lockit.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassMeterScreen(onBack: () -> Unit) {
    var password by remember { mutableStateOf("") }
    
    val strength = remember(password) {
        when {
            password.isEmpty() -> 0f
            password.length < 6 -> 0.2f
            password.any { it.isDigit() } && password.any { it.isUpperCase() } && password.length >= 8 -> 1f
            password.length >= 8 -> 0.7f
            else -> 0.4f
        }
    }
    
    val animatedStrength by animateFloatAsState(
        targetValue = strength,
        animationSpec = tween(durationMillis = 1000)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PassMeter") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .padding(24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.try_strength), fontSize = 14.sp)

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text(stringResource(R.string.type_password_here)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            shape = MaterialTheme.shapes.extraLarge,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(R.string.password_strength), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            if (password.isNotEmpty()) {
                Text(
                    text = when {
                        strength < 0.3f -> stringResource(R.string.strength_weak)
                        strength < 0.7f -> stringResource(R.string.strength_medium)
                        else -> stringResource(R.string.strength_strong)
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        strength < 0.3f -> Color(0xFFE57373)
                        strength < 0.7f -> Color(0xFFFFB74D)
                        else -> Color(0xFF81C784)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = { animatedStrength },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
                .height(4.dp),
            color = when {
                strength < 0.3f -> Color(0xFFE57373)
                strength < 0.7f -> Color(0xFFFFB74D)
                else -> Color(0xFF81C784)
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
    }
}
