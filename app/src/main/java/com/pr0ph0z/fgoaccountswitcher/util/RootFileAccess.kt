package com.pr0ph0z.fgoaccountswitcher.util

import android.content.Context
import java.io.File
import java.io.IOException
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RootFileAccess {
    private val FGO_PACKAGE_NAME = "com.aniplex.fategrandorder.en"
    private val authFiles = arrayOf(
        "54cc790bf952ea710ed7e8be08049531",
        "969b46577f365fadeb79ef14cf5d6370",
        "644b05165c512739dc5e70ad513548fe",
        "e1a9f8e0ff970cc15b1a1d1e31d146db"
    )
    private val rootCommand = arrayOf("su", "-c")


    suspend fun checkRootAccess(): Boolean = suspendCoroutine { continuation ->
        try {
            val process = Runtime.getRuntime().exec("su")
            process.outputStream.close()
            val exitCode = process.waitFor()
            continuation.resume(exitCode == 0)
        } catch (e: IOException) {
            continuation.resume(false)
        }
    }

    suspend fun getAppDataPath(packageName: String): String = suspendCoroutine { continuation ->
        try {
            val command = rootCommand + "pm path $packageName"
            val process = Runtime.getRuntime().exec(command)
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val path = output.trim().removePrefix("package:")
            if (path.isNotEmpty()) {
                val dataDir = File(path).parentFile?.parentFile?.absolutePath
                if (dataDir != null) {
                    continuation.resume(dataDir)
                } else {
                    continuation.resumeWithException(IOException("Unable to determine data directory"))
                }
            } else {
                continuation.resumeWithException(IOException("Package not found"))
            }
        } catch (e: IOException) {
            continuation.resumeWithException(e)
        }
    }

    suspend fun readFile(filePath: String): String = suspendCoroutine { continuation ->
        try {
            val command = rootCommand + "cat $filePath"
            val process = Runtime.getRuntime().exec(command)
            val output = process.inputStream.bufferedReader().use { it.readText() }
            continuation.resume(output)
        } catch (e: IOException) {
            continuation.resumeWithException(e)
        }
    }

    suspend fun writeFile(filePath: String, content: String): Boolean = suspendCoroutine { continuation ->
        try {
            val tempFile = File.createTempFile("temp", null)
            tempFile.writeText(content)
            val command = rootCommand + "cp ${tempFile.absolutePath} $filePath"
            val process = Runtime.getRuntime().exec(command)
            val exitCode = process.waitFor()
            tempFile.delete()
            continuation.resume(exitCode == 0)
        } catch (e: IOException) {
            continuation.resumeWithException(e)
        }
    }

    suspend fun createAccount(context: Context, name: String): Void = suspendCoroutine { continuation ->
        // create the directory
        val externalFileDirectory = context.getExternalFilesDir(null)
        val f = File(externalFileDirectory!!.absolutePath, name)
        f.mkdir()

        try {
            val files = authFiles.map { it -> File(externalFileDirectory!!.absolutePath.replace(context.packageName, FGO_PACKAGE_NAME), "data/$it").absolutePath }
            val command = rootCommand + "cp ${files.joinToString(separator = " ")} ${f.absolutePath}"
            val process = Runtime.getRuntime().exec(command)
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                val output = process.inputStream.bufferedReader().use { it.readText() }
                throw IOException("Error in copying files: $output")
            }
        } catch (e: IOException) {
            continuation.resumeWithException(e)
        }
    }

    suspend fun getCurrentUserID(context: Context): String = suspendCoroutine { continuation ->
        val externalFileDirectory = context.getExternalFilesDir(null)
        val filePath = File(externalFileDirectory!!.absolutePath.replace(context.packageName, FGO_PACKAGE_NAME), "data/e1a9f8e0ff970cc15b1a1d1e31d146db")
        try {
            val command = rootCommand + "cat $filePath"
            val process = Runtime.getRuntime().exec(command)
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val userID = output.replace("\t", "")
            continuation.resume(userID)
        } catch (e: IOException) {
            continuation.resumeWithException(e)
        }
    }
}