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

    suspend fun fetchAccounts(token: String): List<BankAccount> {
        val response = api.getClientInfo(token)
        val accounts = mutableListOf<BankAccount>()
        response.accounts.forEach { acc ->
            val symbol = CurrencyMapper.getSymbol(acc.currencyCode)
            val pan = acc.maskedPan.firstOrNull()?.let { if (it.length >= 4) "*${it.takeLast(4)}" else "" } ?: ""
            accounts.add(BankAccount(
                id = acc.id,
                provider = "Monobank",
                name = "Картка $symbol $pan".trim(),
                type = acc.type,
                balance = acc.balance / 100.0,
                currencyCode = acc.currencyCode
            ))
        }
        response.jars?.forEach { jar ->
            accounts.add(BankAccount(
                id = jar.id, provider = "Monobank", name = "Банка: ${jar.title}",
                type = "jar", balance = jar.balance / 100.0, currencyCode = jar.currencyCode
            ))
        }
        return accounts
    }

    suspend fun fetchTransactionsForAccount(
        token: String,
        accountId: String,
        accountCurrency: Int,
        fromTimeSeconds: Long
    ): List<Transaction> {
        val toTime = (System.currentTimeMillis() / 1000) - 60

        val monoList = api.getStatement(token, accountId, fromTimeSeconds, toTime)

        return monoList.map { monoTx ->
            Transaction(
                id = monoTx.id,
                accountId = accountId,
                amount = Math.abs(monoTx.amount / 100.0),
                currencyCode = accountCurrency,
                description = monoTx.description,
                timestamp = monoTx.time * 1000,
                bankName = "Monobank",
                isExpense = monoTx.amount < 0,
                category = MccDirectory.getCategory(monoTx.mcc),
                mcc = monoTx.mcc
            )
        }
    }
}