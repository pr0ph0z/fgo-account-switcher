package com.pr0ph0z.fgoaccountswitcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AccountRepository
    val allAccounts: StateFlow<List<Account>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AccountRepository(database.accountDao())
        allAccounts = repository.allAccounts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
                AccountViewModel(application)
            }
        }
    }

    fun insert(name: String, userID: String) {
        viewModelScope.launch {
            val dao = AppDatabase.getDatabase(getApplication()).accountDao()
            dao.insert(Account(name = name, userID = userID))
        }
    }

}