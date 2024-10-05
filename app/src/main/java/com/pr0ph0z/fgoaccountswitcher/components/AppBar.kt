package com.pr0ph0z.fgoaccountswitcher.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AppBar(title: String) {
    @OptIn(ExperimentalMaterial3Api::class)
    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xff191c20),
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    )
}