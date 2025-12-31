package com.denisshulika.fincentra.data.network.monobank

import android.util.Log
import com.denisshulika.fincentra.data.models.BankAccount
import com.denisshulika.fincentra.data.models.Transaction
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import com.denisshulika.fincentra.data.network.common.MccDirectory
import com.denisshulika.fincentra.di.DependencyProvider
import kotlin.math.abs

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

    suspend fun fetchAccounts(token: String): List<BankAccount> {
        return try {
            val response = api.getClientInfo(token)
            val accounts = mutableListOf<BankAccount>()

            response.accounts.forEach { acc ->
                val typeName = when(acc.type) {
                    "black" -> "Чорна"
                    "white" -> "Біла"
                    "fop" -> "ФОП"
                    else -> "Картка"
                }

                val currency = CurrencyMapper.getSymbol(acc.currencyCode)

                val pan = acc.maskedPan.firstOrNull()?.let {
                    if (it.length >= 4) "*${it.takeLast(4)}" else ""
                } ?: ""

                accounts.add(
                    BankAccount(
                        id = acc.id,
                        provider = "Monobank",
                        name = "$typeName $currency $pan".trim(),
                        type = acc.type,
                        balance = acc.balance / 100.0,
                        currencyCode = acc.currencyCode
                    )
                )
            }

            response.jars?.forEach { jar ->
                val currency = CurrencyMapper.getSymbol(jar.currencyCode)
                accounts.add(
                    BankAccount(
                        id = jar.id,
                        provider = "Monobank",
                        name = "Банка: ${jar.title} ($currency)",
                        type = "jar",
                        balance = jar.balance / 100.0,
                        currencyCode = jar.currencyCode
                    )
                )
            }
            accounts
        } catch (e: Exception) {
            Log.e("MONO", "Помилка: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchTransactionsForAccount(
        token: String,
        accountId: String,
        days: Int = 30
    ): List<Transaction> {
        return try {
            val toTime = System.currentTimeMillis() / 1000
            val fromTime = toTime - (days * 24 * 60 * 60)

            val monoList = api.getStatement(token, accountId, fromTime, toTime)

            monoList.map { monoTx ->
                Transaction(
                    id = monoTx.id,
                    accountId = accountId,
                    amount = abs(monoTx.amount / 100.0),
                    currencyCode = monoTx.currencyCode,
                    description = monoTx.description,
                    timestamp = monoTx.time * 1000,
                    bankName = "Monobank",
                    isExpense = monoTx.amount < 0,
                    category = MccDirectory.getCategory(monoTx.mcc),
                    mcc = monoTx.mcc
                )
            }
        } catch (e: Exception) {
            Log.e("MONO", "Помилка для рахунку $accountId: ${e.message}")
            emptyList()
        }
    }
}