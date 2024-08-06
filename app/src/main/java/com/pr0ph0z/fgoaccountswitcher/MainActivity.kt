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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.core.content.IntentCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pr0ph0z.fgoaccountswitcher.ui.theme.FGOAccountSwitcherTheme
import com.pr0ph0z.fgoaccountswitcher.util.Util
import com.pr0ph0z.fgoaccountswitcher.util.Util.Companion.isPackageInstalled


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FGOAccountSwitcherTheme {
                val viewModel: AccountViewModel = viewModel(factory = AccountViewModel.Factory)
                var showDialog by remember { mutableStateOf(false) }
                var newAccountName by remember { mutableStateOf("") }
                var FILE_MANAGER = "com.google.android.documentsui"

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
                            if (isPackageInstalled(applicationContext, FILE_MANAGER)) {
                                val intent = Intent()
                                val activityName = "com.android.documentsui.files.FilesActivity"

                                intent.component = ComponentName(FILE_MANAGER, activityName)

                                try {
                                    startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    Log.e("FileManager", "startFilesManager: failed to start", e);
                                }

                            } else {
                                val selectedUri = Uri.parse(
                                    Environment.getExternalStorageDirectory().toString()
                                )
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.setDataAndType(selectedUri, "resource/folder")

                                if (intent.resolveActivityInfo(packageManager, 0) != null) {
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(this, "No file manager app found", Toast.LENGTH_LONG).show()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                ) {innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        MainScreen(viewModel)
                    }
                }

                when {
                    intent?.action == Intent.ACTION_SEND_MULTIPLE -> {
                        Util.handleMultipleImage(intent, applicationContext)
                    }
                }


                if (showDialog) {
                    FileContentDialog(
                        onDismiss = { showDialog = false }
                    )

                }

//                    Dialog(onDismissRequest = { showDialog = false }) {
//                        Surface(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(16.dp),
//                            shape = MaterialTheme.shapes.medium,
//                            color = MaterialTheme.colorScheme.surface
//                        ) {
//                            Column(modifier = Modifier.padding(16.dp)) {
//                                Text("Add New Account", style = MaterialTheme.typography.headlineSmall)
//                                Spacer(modifier = Modifier.height(16.dp))
//                                OutlinedTextField(
//                                    value = newAccountName,
//                                    onValueChange = { newAccountName = it },
//                                    label = { Text("Account Name") },
//                                    modifier = Modifier.fillMaxWidth()
//                                )
//                                Spacer(modifier = Modifier.height(16.dp))
//                                Row(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    horizontalArrangement = Arrangement.End
//                                ) {
//                                    TextButton(onClick = { showDialog = false }) {
//                                        Text("Cancel")
//                                    }
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    Button(
//                                        onClick = {
//                                            if (newAccountName.isNotBlank()) {
//                                                viewModel.insert(newAccountName, "")
//                                                newAccountName = ""
//                                                showDialog = false
//                                            }
//                                        }
//                                    ) {
//                                        Text("Add")
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: AccountViewModel = viewModel()) {
    val accounts by viewModel.allAccounts.collectAsState()

    SimpleListView(accounts)
}

@Composable
fun SimpleListView(accounts: List<Account>) {
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
                ListItem(account.name)
                Divider(
                    color = Color.LightGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun ListItem(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
fun FileContentDialog(
    fileName: String = "bruh",
    content: String = "content",
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = content)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

