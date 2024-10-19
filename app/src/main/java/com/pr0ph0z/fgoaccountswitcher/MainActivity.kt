package com.pr0ph0z.fgoaccountswitcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pr0ph0z.fgoaccountswitcher.components.AccountDeleteDialog
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
    private lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent>
    private val accountManager = AccountManager(rootFileAccess)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the launcher to handle the overlay permission result
        overlayPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        // Check for the permission on startup or when needed
        checkOverlayPermission()


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
                                appViewModel.updateDialog(Dialog.FORM)
                            }, onDelete = {
                                appViewModel.updateDialog(Dialog.DELETE)
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            appViewModel.updateDialog(Dialog.FORM)
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
                                    accountManager.switchAccount(applicationContext, account)
                                    startFloatingWidget(accounts)
                                    minimizeApp()
                                }
                            },
                            onItemLongClick = { account ->
                                appViewModel.updateSelectedAccount(account)
                            })
                    }
                }

                when (appUiState.dialog) {
                    Dialog.FORM -> {
                        AccountFormDialog(
                            accountName = accountName,
                            formMode = appUiState.formMode,
                            onDismiss = {
                                appViewModel.updateDialog(Dialog.NO_DIALOG)
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

                    Dialog.DELETE -> {
                        AccountDeleteDialog(
                            onDismiss = { appViewModel.updateDialog(Dialog.NO_DIALOG) },
                            onDelete = { deleteAccount(appUiState.selectedAccount, viewModel, appViewModel) })
                    }

                    else -> {}
                }
            }
        }
    }

    // TODO: Add an option to delete the authentication files as well
    private fun deleteAccount(
        account: Account,
        accountViewModel: AccountViewModel,
        appViewModel: AppViewModel
    ) {
        lifecycleScope.launch {
            accountViewModel.delete(account)
        }
        appViewModel.updateSelectedAccount(Account())
        appViewModel.updateDialog(Dialog.NO_DIALOG)
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
                            Account(
                                id = account.id,
                                name = accountName.value,
                                userID = account.userID
                            )
                        )
                    }
                }
            }
            appViewModel.updateSelectedAccount(Account())
            appViewModel.updateDialog(Dialog.NO_DIALOG)
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

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            overlayPermissionLauncher.launch(intent)
        }
    }

    private fun minimizeApp() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(startMain)
    }

    private fun startFloatingWidget(accounts: List<Account>) {
        val intent = Intent(this, FloatingWidgetService::class.java)
        intent.putParcelableArrayListExtra("accounts", ArrayList(accounts))
        startService(intent)
    }
}
