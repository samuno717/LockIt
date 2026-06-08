package com.example.lockit.ui.icons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.lockit.R

/**
 * One selectable icon. [key] is the drawable resource name (e.g. "fi_brands_steam") and is
 * what gets persisted in [com.example.lockit.model.PasswordEntry.iconKey].
 */
data class IconOption(val key: String, val label: String)

object AppIcons {
    const val NONE = ""

    /**
     * Every drawable that looks like an imported icon-pack glyph. Flaticon "Uicons" SVGs,
     * once imported via Android Studio's Vector Asset tool, land as `fi_brands_*`, `fi_rr_*`,
     * `fi_sr_*`, etc. — so whatever the user imports shows up here automatically, with no
     * hard-coded list to maintain.
     */
    fun available(): List<IconOption> {
        return R.drawable::class.java.fields
            .map { it.name }
            .filter { it.startsWith("fi_") }
            .sorted()
            .map { IconOption(key = it, label = prettyLabel(it)) }
    }

    fun labelFor(resName: String): String = prettyLabel(resName)

    private fun prettyLabel(resName: String): String {
        val stripped = resName
            .removePrefix("fi_brands_")
            .removePrefix("fi_rr_")
            .removePrefix("fi_sr_")
            .removePrefix("fi_br_")
            .removePrefix("fi_tr_")
            .removePrefix("fi_")
        return stripped.split("_")
            .filter { it.isNotBlank() }
            .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
    }
}

@Composable
fun rememberAvailableIcons(): List<IconOption> = remember { AppIcons.available() }

/** Resolves a drawable name to its resource id, or 0 when the file is not present. */
@Composable
fun rememberIconResId(resName: String?): Int {
    if (resName.isNullOrBlank()) return 0
    val context = LocalContext.current
    return remember(resName) {
        context.resources.getIdentifier(resName, "drawable", context.packageName)
    }
}

/**
 * Avatar for a saved login: shows the chosen icon when its drawable exists, otherwise
 * falls back to the first letter of the service name.
 */
@Composable
fun ServiceIcon(
    serviceName: String,
    iconKey: String,
    modifier: Modifier = Modifier.size(40.dp)
) {
    val resId = rememberIconResId(iconKey)

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (resId != 0) {
                Icon(
                    painter = painterResource(resId),
                    contentDescription = iconKey,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = serviceName.take(1).uppercase().ifBlank { "?" },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Compact icon selector: shows the current choice and opens a searchable grid dialog.
 * Scales to hundreds of imported icons.
 */
@Composable
fun IconPicker(
    selectedKey: String,
    onSelected: (String) -> Unit
) {
    var dialogOpen by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { dialogOpen = true }
    ) {
        ServiceIcon(serviceName = "?", iconKey = selectedKey)
        Text(
            text = if (selectedKey.isBlank()) stringResource(R.string.choose_icon) else AppIcons.labelFor(selectedKey),
            modifier = Modifier.padding(start = 12.dp)
        )
    }

    if (dialogOpen) {
        IconPickerDialog(
            selectedKey = selectedKey,
            onSelected = {
                onSelected(it)
                dialogOpen = false
            },
            onDismiss = { dialogOpen = false }
        )
    }
}

@Composable
private fun IconPickerDialog(
    selectedKey: String,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val icons = rememberAvailableIcons()
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, icons) {
        if (query.isBlank()) icons
        else icons.filter { it.label.contains(query, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.choose_icon), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.size(12.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.search)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.size(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 64.dp),
                    modifier = Modifier.heightIn(max = 360.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        IconPickerCell(
                            selected = selectedKey == AppIcons.NONE,
                            label = stringResource(R.string.none),
                            onClick = { onSelected(AppIcons.NONE) }
                        ) {
                            Text(text = "Aa", fontWeight = FontWeight.Bold)
                        }
                    }
                    items(filtered) { option ->
                        val resId = rememberIconResId(option.key)
                        IconPickerCell(
                            selected = selectedKey == option.key,
                            label = option.label,
                            onClick = { onSelected(option.key) }
                        ) {
                            if (resId != 0) {
                                Icon(
                                    painter = painterResource(resId),
                                    contentDescription = option.label,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(text = option.label.take(1), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.size(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
                }
            }
        }
    }
}

@Composable
private fun IconPickerCell(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = if (selected) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface)
            } else {
                BorderStroke(1.dp, Color.Transparent)
            }
        ) {
            Box(contentAlignment = Alignment.Center) { content() }
        }
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
