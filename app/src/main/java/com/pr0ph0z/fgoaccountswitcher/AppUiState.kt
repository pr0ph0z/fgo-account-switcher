package com.pr0ph0z.fgoaccountswitcher

enum class FormMode {
    CREATE, EDIT
}

data class AppUiState(
    val showDialog: Boolean = false,
    val selectedAccount: Account = Account(),
    val formMode: FormMode = FormMode.CREATE
)