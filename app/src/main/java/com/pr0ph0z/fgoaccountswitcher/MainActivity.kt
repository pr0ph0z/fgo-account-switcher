package com.pr0ph0z.fgoaccountswitcher

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pr0ph0z.fgoaccountswitcher.components.AccountFormDialog
import com.pr0ph0z.fgoaccountswitcher.components.AppBar
import com.pr0ph0z.fgoaccountswitcher.components.ListView
import com.pr0ph0z.fgoaccountswitcher.ui.theme.FGOAccountSwitcherTheme
import com.pr0ph0z.fgoaccountswitcher.util.RootFileAccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    private val rootFileAccess = RootFileAccess()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkRootAccess()

        setContent {
            FGOAccountSwitcherTheme {
                val viewModel: AccountViewModel = viewModel(factory = AccountViewModel.Factory)
                val appViewModel: AppViewModel = viewModel()
                val appUiState by appViewModel.uiState.collectAsState()
                val accountName = remember { mutableStateOf("") }
                val accounts by viewModel.allAccounts.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        AppBar(
                            title = "FGO Account Switcher",
                            appUiState,
                            onEdit = {
                                appViewModel.updateFormMode(FormMode.EDIT)
                                accountName.value = appUiState.selectedAccount.name
                                appViewModel.updateDialog(true)
                            }, onDelete = {
                            //
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            appViewModel.updateDialog(true)
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        ListView(
                            accounts,
                            appUiState.selectedAccount,
                            onItemClick = { account ->
                                if (appUiState.selectedAccount.id == account.id) {
                                    appViewModel.updateSelectedAccount(Account())
                                } else {
                                    switchAccount(account)
                                }
                            },
                            onItemLongClick = { account ->
                                appViewModel.updateSelectedAccount(account)
                            })
                    }
                }

                if (appUiState.showDialog) {
                    AccountFormDialog(
                        accountName = accountName,
                        formMode = appUiState.formMode,
                        onDismiss = {
                            appViewModel.updateDialog(false)
                            accountName.value = ""
                        },
                        onSave = {
                            saveAccount(
                                accountName,
                                appUiState.selectedAccount,
                                appUiState.formMode,
                                viewModel,
                                appViewModel
                            )
                        })
                }
            }
        }
    }

    private fun switchAccount(account: Account) {
        lifecycleScope.launch {
            rootFileAccess.switchAccount(applicationContext, account.userID)
        }
        Toast.makeText(
            applicationContext,
            "Account switched to ${account.name}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun saveAccount(
        accountName: MutableState<String>,
        account: Account,
        formMode: FormMode,
        accountViewModel: AccountViewModel,
        appViewModel: AppViewModel
    ) {
        if (accountName.value.isNotBlank()) {
            lifecycleScope.launch {
                val currentUserID = rootFileAccess.getCurrentUserID(applicationContext)
                when (formMode) {
                    FormMode.CREATE -> {
                        accountViewModel.insert(accountName.value, currentUserID)
                        rootFileAccess.createAccount(applicationContext, currentUserID)
                    }
                    FormMode.EDIT -> {
                        accountViewModel.update(
                            Account(id = account.id, name = accountName.value, userID = account.userID)
                        )
                    }
                }
            }
            appViewModel.updateSelectedAccount(Account())
            appViewModel.updateDialog(false)
        }
    }

    private fun checkRootAccess() {
        lifecycleScope.launch {
            val hasRootAccess = withContext(Dispatchers.IO) {
                rootFileAccess.checkRootAccess()
            }

            if (hasRootAccess) {
                handleRootAccess()
            } else {
                handleNoRootAccess()
            }
        }
    }

    private fun handleRootAccess() {
        Toast.makeText(applicationContext, "Root granted", Toast.LENGTH_SHORT).show()
    }

    private fun handleNoRootAccess() {
        Toast.makeText(applicationContext, "Root is not granted", Toast.LENGTH_SHORT).show()
    }

}
