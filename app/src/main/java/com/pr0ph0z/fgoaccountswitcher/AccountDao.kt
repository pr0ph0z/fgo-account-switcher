package com.pr0ph0z.fgoaccountswitcher

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER by id ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Insert
    suspend fun insert(account: Account)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(model: Account)
}