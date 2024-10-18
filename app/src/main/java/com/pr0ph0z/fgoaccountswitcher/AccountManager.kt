package com.pr0ph0z.fgoaccountswitcher

import android.content.Context
import android.widget.Toast
import com.pr0ph0z.fgoaccountswitcher.util.RootFileAccess
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AccountManager(rfa: RootFileAccess) {
    private val rootFileAccess = rfa
    fun switchAccount(context: Context, account: Account) {
        GlobalScope.launch {
            rootFileAccess.switchAccount(context, account.userID)
        }
        Toast.makeText(
            context,
            "Account switched to ${account.name}",
            Toast.LENGTH_SHORT
        ).show()
    }
}