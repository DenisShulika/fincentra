package com.denisshulika.fincentra.data.network

import android.util.Log
import com.denisshulika.fincentra.data.models.MccDirectory
import com.denisshulika.fincentra.data.models.Transaction
import com.denisshulika.fincentra.data.models.TransactionCategory
import com.denisshulika.fincentra.di.DependencyProvider

class MonobankService {
    private val api = DependencyProvider.monobankApi

    suspend fun fetchAllTransactions(token: String): List<Transaction> {
        return try {
            val clientInfo = api.getClientInfo(token)

            val secInDay = 86400L
            val fromTime = (System.currentTimeMillis() / 1000) - (30 * secInDay)
            val toTime = System.currentTimeMillis() / 1000

            val allTransactions = mutableListOf<Transaction>()

            val accountToSync = clientInfo.accounts.firstOrNull { it.type == "black" }
                ?: clientInfo.accounts.firstOrNull()

            if (accountToSync != null) {
                val monoList = api.getStatement(token, accountToSync.id, fromTime, toTime)

                Log.d("MONO", "Отримано транзакцій: ${monoList.size}")

                monoList.forEach { monoTx ->
                    allTransactions.add(
                        Transaction(
                            id = monoTx.id,
                            accountId = accountToSync.id,
                            amount = Math.abs(monoTx.amount / 100.0),
                            currencyCode = accountToSync.currencyCode,
                            description = monoTx.description,
                            timestamp = monoTx.time * 1000,
                            bankName = "Monobank",
                            isExpense = monoTx.amount < 0,
                            category = MccDirectory.getCategory(monoTx.mcc),
                            mcc = monoTx.mcc
                        )
                    )
                }
            }
            allTransactions
        } catch (e: Exception) {
            Log.e("MONO", "Помилка: ${e.message}")
            emptyList()
        }
    }
}