package com.pr0ph0z.fgoaccountswitcher

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER by id ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Insert
    suspend fun insert(account: Account)

}