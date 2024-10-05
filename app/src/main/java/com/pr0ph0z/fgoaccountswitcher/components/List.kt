package com.pr0ph0z.fgoaccountswitcher.components

import android.util.Log.i
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pr0ph0z.fgoaccountswitcher.Account
import com.pr0ph0z.fgoaccountswitcher.AppViewModel

@Composable
fun ListItem(account: Account, onItemClick: (Account) -> Unit) {
    Text(
        text = account.name,
        modifier = Modifier
            .clickable { onItemClick(account) }
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListItem(
    account: Account,
    isSelected: Boolean,
    onItemClick: (Account) -> Unit,
    onItemLongClick: (Account) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(if (isSelected) Color.DarkGray else Color.Transparent)
            .combinedClickable(
                onClick = { onItemClick(account) },
                onLongClick = { onItemLongClick(account) }
            )
            .padding(16.dp)
    ) {
        Column {
            Text(text = account.name)
            Text(text = "(${account.userID.dropLast(1).replace(Regex(".{3}")){
                "${it.value},"
            } + account.userID.last()})", fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun ListView(
    accounts: List<Account>,
    selectedAccount: Account,
    onItemClick: (Account) -> Unit,
    onItemLongClick: (Account) -> Unit)
{
    if (accounts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn {
            items(accounts) { account ->
                ListItem(account, isSelected = selectedAccount.id == account.id, onItemClick, onItemLongClick)
            }
        }
    }
}