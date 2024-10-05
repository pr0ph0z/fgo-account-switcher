package com.pr0ph0z.fgoaccountswitcher.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import com.pr0ph0z.fgoaccountswitcher.AppViewModel
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeoutException

class Util {
    companion object {
        fun isPackageInstalled(context: Context, packageName: String?): Boolean {
            var result = false
            try {
                context.packageManager.getPackageInfo(packageName!!, PackageManager.GET_ACTIVITIES)
                result = true
            } catch (e: PackageManager.NameNotFoundException) {
                Log.d("busted", "lol")
            }
            return result
        }

        fun handleCredentialFiles(intent: Intent, context: Context, appViewModel: AppViewModel) {
            val fileList: ArrayList<Uri>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            }
            if (fileList != null) {
                appViewModel.updateDialog(true)
                for (uri in fileList) {
                    try {
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            val text = inputStream.bufferedReader().use { it.readText() }
                            Log.d("FileContent", "Content of ${getFileName(uri, context)}: $text")
                        }
                    } catch (e: Exception) {
                        Log.e("FileContent", "Error reading file: ${e.message}")
                    }
                }
            }
        }

        private fun getFileName(uri: Uri, context: Context): String? {
            var result: String? = null
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (columnIndex != -1) {
                            result = it.getString(columnIndex)
                        }
                    }
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result?.lastIndexOf('/')
                if (cut != -1) {
                    result = result?.substring(cut!! + 1)
                }
            }
            return result
        }

        fun grantRootAccess(context: Context): Boolean {
            val isRooted = try {
                Runtime.getRuntime().exec("su")
                true
            } catch (e: IOException) {
                false
            } catch (e: TimeoutException) {
                false
            }

            Thread.sleep(1000)

            if (isRooted) {
                Toast.makeText(context, "Root access granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Root access granted is not granted", Toast.LENGTH_SHORT).show()
            }

            return isRooted
        }

        fun getRootDirectory(): File {
            return Environment.getRootDirectory()
        }

        fun readRootFile(path: String): String {
            return File(path).readText()
        }

        fun writeRootFile(path: String, data: String) {
            File(getRootDirectory(), path).writeText(data)
        }

        fun deleteRootFile(path: String) {
            File(getRootDirectory(), path).delete()
        }

    }
}