package com.pr0ph0z.fgoaccountswitcher.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.pr0ph0z.fgoaccountswitcher.AppUiState

@Composable
fun AppBar(title: String, appUiState: AppUiState, onEdit: () -> Unit, onDelete: () -> Unit) {
    val isActionMode = appUiState.selectedAccount.id != 0

    if (isActionMode) {
        ActionModeTopBar(onEdit, onDelete)
    } else {
        DefaultTopBar(title)
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopBar(title: String) {
    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xff191c20),
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionModeTopBar(
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = {},
        actions = {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    )
}