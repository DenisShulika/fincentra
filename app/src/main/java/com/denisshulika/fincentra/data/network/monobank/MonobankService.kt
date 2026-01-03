package com.denisshulika.fincentra.data.network.monobank

import android.util.Log
import com.denisshulika.fincentra.data.models.BankAccount
import com.denisshulika.fincentra.data.models.Transaction
import com.denisshulika.fincentra.data.network.common.BankProvider
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import com.denisshulika.fincentra.data.network.common.MccDirectory
import com.denisshulika.fincentra.data.network.monobank.models.MonoTransactionResponse
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.delay
import kotlin.math.abs

class MonobankService : BankProvider {
    private val api get() = DependencyProvider.monobankApi


    override suspend fun fetchAccounts(token: String): List<BankAccount> {
        return try {
            val response = api.getClientInfo(token)
            val accounts = mutableListOf<BankAccount>()
            response.accounts.forEach { acc ->
                val symbol = CurrencyMapper.getCodeName(acc.currencyCode)
                val pan = acc.maskedPan.firstOrNull()?.let { if (it.length >= 4) "*${it.takeLast(4)}" else "" } ?: ""
                accounts.add(BankAccount(
                    id = acc.id, provider = "Monobank", name = "Картка $symbol $pan".trim(),
                    type = acc.type, balance = acc.balance / 100.0, currencyCode = acc.currencyCode
                ))
            }
            response.jars?.forEach { jar ->
                accounts.add(BankAccount(
                    id = jar.id, provider = "Monobank", name = "Банка: ${jar.title}",
                    type = "jar", balance = jar.balance / 100.0, currencyCode = jar.currencyCode
                ))
            }
            accounts
        } catch (e: Exception) {
            Log.e("MONO_API", "Помилка API (можливо 429): ${e.message}")
            emptyList()
        }
    }

    override suspend fun fetchTransactionsForAccount(
        token: String,
        accountId: String,
        accountCurrency: Int,
        fromTimeSeconds: Long,
        onProgress: suspend (String) -> Unit,
        onBatchLoaded: suspend (List<Transaction>) -> Unit
    ): List<Transaction> {
        val allTransactions = mutableListOf<Transaction>()
        val now = System.currentTimeMillis() / 1000

        val maxHistory = 2682000L
        val finalFrom = if (now - fromTimeSeconds > maxHistory || fromTimeSeconds == 0L) {
            now - maxHistory
        } else {
            fromTimeSeconds
        }

        var currentTo = now - 60
        var shouldContinue = true

        while (shouldContinue) {
            Log.d("MONO_SYNC", "Запит пачки: From=$finalFrom To=$currentTo")

            val monoList = try {
                api.getStatement(token, accountId, finalFrom, currentTo)
            } catch (e: Exception) {
                Log.e("MONO_SYNC", "Помилка API: ${e.message}")
                emptyList()
            }

            if (monoList.isEmpty()) {
                shouldContinue = false
            } else {
                val mapped = monoList.map { it.toDomainModel(accountId, accountCurrency) }

                onBatchLoaded(mapped)
                allTransactions.addAll(mapped)

                if (monoList.size == 500) {
                    currentTo = monoList.last().time

                    for (i in 60 downTo 1) {
                        onProgress("Багато даних... Наступна пачка через ${i}с")
                        delay(1000)
                    }
                } else {
                    shouldContinue = false
                }
            }
        }
        return allTransactions
    }

    private fun MonoTransactionResponse.toDomainModel(accountId: String, accountCurrency: Int): Transaction {
        return Transaction(
            id = this.id,
            accountId = accountId,
            amount = abs(this.amount / 100.0),
            currencyCode = accountCurrency,
            description = this.description,
            timestamp = this.time * 1000,
            bankName = "Monobank",
            isExpense = this.amount < 0,
            category = MccDirectory.getCategory(this.mcc),
            mcc = this.mcc,
            balance = this.balance / 100.0,
            comment = this.comment
        )
    }
}