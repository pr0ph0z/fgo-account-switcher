package com.pr0ph0z.fgoaccountswitcher

enum class FormMode {
    CREATE, EDIT
}

enum class Dialog {
    FORM, DELETE, NO_DIALOG
}

data class AppUiState(
    val dialog: Dialog = Dialog.NO_DIALOG,
    val selectedAccount: Account = Account(),
    val formMode: FormMode = FormMode.CREATE
)