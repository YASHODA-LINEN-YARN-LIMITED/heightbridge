package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SearchableDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    readOnly: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val filteredOptions = remember(options, searchText, readOnly) {
        if (searchText.isBlank() || readOnly) options
        else {
            val query = searchText.trim().lowercase()
            options.filter { it.lowercase().contains(query) }
        }
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = if (expanded && !readOnly) searchText else selectedOption,
            onValueChange = { newValue ->
                if (!readOnly) {
                    searchText = newValue
                    onOptionSelected(newValue)
                    if (!expanded) expanded = true
                }
            },
            readOnly = readOnly,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = {
                    expanded = !expanded
                    if (expanded && !readOnly) searchText = selectedOption
                }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Toggle Dropdown",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            leadingIcon = if (!readOnly) {
                {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF111827),
                unfocusedTextColor = Color(0xFF111827),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && !expanded) {
                        expanded = true
                        if (!readOnly) searchText = selectedOption
                    }
                }
                .testTag("dropdown_$label")
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 260.dp)
                .background(Color.White)
        ) {
            if (filteredOptions.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (searchText.isBlank()) "No options available" else "Use entered: '$searchText'",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF475569)
                        )
                    },
                    onClick = {
                        onOptionSelected(searchText)
                        expanded = false
                    }
                )
            } else {
                filteredOptions.take(80).forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (item == selectedOption) FontWeight.Bold else FontWeight.Medium,
                                color = if (item == selectedOption) MaterialTheme.colorScheme.primary else Color(0xFF111827)
                            )
                        },
                        onClick = {
                            onOptionSelected(item)
                            expanded = false
                            searchText = ""
                        }
                    )
                }
            }
        }
    }
}
