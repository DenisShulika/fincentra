package com.denisshulika.fincentra.data.network.common

import com.denisshulika.fincentra.data.models.BankAccount
import com.denisshulika.fincentra.data.models.Transaction

interface BankProvider {
    suspend fun fetchAccounts(token: String): List<BankAccount>

    suspend fun fetchTransactionsForAccount(
        token: String,
        accountId: String,
        accountCurrency: Int,
        fromTimeSeconds: Long,
        onProgress: suspend (String) -> Unit,
        onBatchLoaded: suspend (List<Transaction>) -> Unit
    ): List<Transaction>
}