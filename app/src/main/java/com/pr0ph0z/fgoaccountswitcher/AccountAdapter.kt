package com.pr0ph0z.fgoaccountswitcher

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView

class AccountAdapter(context: Context, private val resource: Int, private val accounts: List<Account>) :
    ArrayAdapter<Account>(context, resource, accounts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val parentView: View = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        val view = parentView.findViewById<LinearLayout>(R.id.inner_lv_row)

        val account = accounts[position]
        val accountNameTextView: TextView = view.findViewById(R.id.tv_account_name)
        val userIDTextView: TextView = view.findViewById(R.id.tv_user_id )

        accountNameTextView.text = account.name
        userIDTextView.text = "(${account.userID.dropLast(1).replace(Regex(".{3}")){
            "${it.value},"
        } + account.userID.last()})"

        return view
    }
}
