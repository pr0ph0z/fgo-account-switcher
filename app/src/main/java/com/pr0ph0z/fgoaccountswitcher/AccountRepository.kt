package com.pr0ph0z.fgoaccountswitcher

import kotlinx.coroutines.flow.Flow

class AccountRepository(private val accountDao: AccountDao) {
    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()
}