package com.denisshulika.fincentra.data.util

import com.denisshulika.fincentra.data.models.domain.Transaction

object TransactionFilterEngine {

    fun filter(
        transactions: List<Transaction>,
        selectedAccountIds: List<String>,
        query: String,
        bankFilter: String,
        typeFilter: String,
        selectedCats: Set<String>,
        dateRange: LongRange?
    ): List<Transaction> {
        return transactions
            .filterByActiveAccounts(selectedAccountIds)
            .filterBySearch(query)
            .filterByBank(bankFilter)
            .filterByType(typeFilter)
            .filterByCategories(selectedCats)
            .filterByDate(dateRange)
    }

    private fun List<Transaction>.filterByActiveAccounts(ids: List<String>) = filter {
        if (it.accountId == TransactionConstants.ACCOUNT_ID_MANUAL) return@filter true
        ids.contains(it.accountId)
    }

    private fun List<Transaction>.filterBySearch(query: String): List<Transaction> {
        if (query.isBlank()) return this
        val trimmed = query.trim().lowercase()
        return filter { tx ->
            val amountFilter = when {
                trimmed.startsWith(">") -> trimmed.drop(1).toDoubleOrNull()
                trimmed.startsWith("<") -> trimmed.drop(1).toDoubleOrNull()
                else -> null
            }
            if (amountFilter != null) {
                if (trimmed.startsWith(">")) tx.amount > amountFilter else tx.amount < amountFilter
            } else {
                tx.description.lowercase().contains(trimmed) ||
                        tx.category.name.lowercase().contains(trimmed)
            }
        }
    }

    private fun List<Transaction>.filterByBank(bank: String) = filter {
        if (bank == FilterConstants.ALL) true
        else it.bankName == bank
    }

    private fun List<Transaction>.filterByType(type: String) = filter {
        when (type) {
            FilterConstants.EXPENSES -> it.isExpense
            FilterConstants.INCOME -> !it.isExpense
            else -> true
        }
    }

    private fun List<Transaction>.filterByCategories(selectedCats: Set<String>) = filter {
        if (selectedCats.isEmpty()) {
            true
        } else {
            selectedCats.contains(it.category.name)
        }
    }

    private fun List<Transaction>.filterByDate(range: LongRange?) = filter {
        if (range == null) true else it.timestamp in range
    }
}