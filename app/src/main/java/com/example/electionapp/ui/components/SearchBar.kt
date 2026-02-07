package com.example.electionapp.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    history: List<String>,
    onHistoryItemClick: (String) -> Unit,
    onDeleteHistoryItem: (String) -> Unit, // New callback
    onSearchExecuted: (String) -> Unit     // To save to history
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxWidth()) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().height(40.dp),
            interactionSource = interactionSource,
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                onSearchExecuted(query) // ✅ Save to history here
                keyboardController?.hide()
                focusManager.clearFocus()
            }),
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = query,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    placeholder = { Text(placeholder, fontSize = 14.sp, color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange(""); focusManager.clearFocus() }) {
                                Icon(Icons.Default.Clear, "Clear", Modifier.size(20.dp))
                            }
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    container = {
                        OutlinedTextFieldDefaults.ContainerBox(
                            enabled = true,
                            isError = false,
                            interactionSource = interactionSource,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF1F4F8),
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                        )
                    }
                )
            }
        )

        DropdownMenu(
            expanded = isFocused && query.isEmpty() && history.isNotEmpty(),
            onDismissRequest = { /* focusManager.clearFocus() would hide keyboard, so we keep empty */ },
            modifier = Modifier.fillMaxWidth(0.9f),
            properties = PopupProperties(focusable = false)
        ) {
            Text("Recent Searches", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            history.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onQueryChange(item)
                        onHistoryItemClick(item)
                        focusManager.clearFocus()
                    },
                    leadingIcon = { Icon(Icons.Default.History, null, Modifier.size(18.dp)) },
                    // ✅ Add Delete Icon
                    trailingIcon = {
                        IconButton(onClick = { onDeleteHistoryItem(item) }) {
                            Icon(Icons.Default.Close, "Delete", Modifier.size(16.dp), tint = Color.Gray)
                        }
                    }
                )
            }
        }
    }
}