package com.pr0ph0z.fgoaccountswitcher

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pr0ph0z.fgoaccountswitcher.components.FileContentDialog
import com.pr0ph0z.fgoaccountswitcher.components.ListView
import com.pr0ph0z.fgoaccountswitcher.ui.theme.FGOAccountSwitcherTheme
import com.pr0ph0z.fgoaccountswitcher.util.RootFileAccess
import com.pr0ph0z.fgoaccountswitcher.util.Util
import com.pr0ph0z.fgoaccountswitcher.util.Util.Companion.isPackageInstalled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class MainActivity : ComponentActivity() {
    private val rootFileAccess = RootFileAccess()
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkRootAccess()

        val packageName = "com.aniplex.fategrandorder"
        val fileName = "e1a9f8e0ff970cc15b1a1d1e31d146db"
        val externalFilesDir = applicationContext.getExternalFilesDir(null)
            val appSpecificDirectory = File(externalFilesDir!!.parent, packageName)
            val filePath = File(appSpecificDirectory, "files${File.separator}data${File.separator}$fileName").absolutePath
            Log.d("fgo-tagpath3", externalFilesDir!!.parent)
            Log.d("fgo-tagpath4", externalFilesDir!!.absolutePath)


        setContent {
            FGOAccountSwitcherTheme {
                val viewModel: AccountViewModel = viewModel(factory = AccountViewModel.Factory)
                val appViewModel: AppViewModel = viewModel()
                val appUiState by appViewModel.uiState.collectAsState()
                var newAccountName by remember { mutableStateOf("") }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("FGO Account Switcher") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xff191c20),
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        )
                    },
                    floatingActionButton = {
                        val context = LocalContext.current
                        FloatingActionButton(onClick = {
                            appViewModel.updateDialog(true)
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                ) {innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        MainScreen(viewModel)
                    }
                }

                if (appUiState.showDialog) {
                    Dialog(onDismissRequest = { appViewModel.updateDialog(false) }) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Add New Account", style = MaterialTheme.typography.headlineSmall)
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = newAccountName,
                                    onValueChange = { newAccountName = it },
                                    label = { Text("Account Name") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { appViewModel.updateDialog(false) }) {
                                        Text("Cancel")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (newAccountName.isNotBlank()) {
                                                createAccount(applicationContext, newAccountName)
                                                appViewModel.updateDialog(false)
                                            }
                                        }
                                    ) {
                                        Text("Add")
                                    }
                            }
                        }
                        }
                    }
                }
            }
        }
    }

    private fun createAccount(context: Context, name: String) {
        lifecycleScope.launch {
            rootFileAccess.createAccount(context, name)
        }
    }

    private fun getAppDataPath(path: String) {
        lifecycleScope.launch {
            val qwe = rootFileAccess.getAppDataPath(path)
//            val file = rootFileAccess.readFile()
            Log.d("fgo-tag", qwe)
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
        Log.d("fgo-tag", "granted")
    }

    private fun handleNoRootAccess() {
        Log.d("fgo-tag", "not granted")
    }

}

@Composable
fun MainScreen(viewModel: AccountViewModel = viewModel()) {
    val accounts by viewModel.allAccounts.collectAsState()

    ListView(accounts)
}