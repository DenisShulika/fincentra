package com.denisshulika.fincentra.data.util

import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.viewmodels.TransactionsViewModel.SortOrder

object TransactionFilterEngine {

    fun filter(
        transactions: List<Transaction>,
        selectedAccountIds: List<String>,
        query: String,
        bankFilter: String,
        typeFilter: String,
        selectedCats: Set<String>,
        dateRange: LongRange?,
        sortOrder: SortOrder
    ): List<Transaction> {
        return transactions
            .filterByActiveAccounts(selectedAccountIds)
            .filterBySearch(query)
            .filterByBank(bankFilter)
            .filterByType(typeFilter)
            .filterByCategories(selectedCats)
            .filterByDate(dateRange)
            .applySort(sortOrder)
    }

    private fun List<Transaction>.filterByActiveAccounts(ids: List<String>) = filter {
        it.accountId == TransactionConstants.ACCOUNT_ID_MANUAL || ids.contains(it.accountId)
    }

    private fun List<Transaction>.filterBySearch(query: String): List<Transaction> {
        if (query.isBlank()) return this
        val trimmedQuery = query.trim()

        return filter { tx ->
            val amountFilter = when {
                trimmedQuery.startsWith(">") -> trimmedQuery.drop(1).toDoubleOrNull()
                trimmedQuery.startsWith("<") -> trimmedQuery.drop(1).toDoubleOrNull()
                else -> null
            }

            if (amountFilter != null) {
                if (trimmedQuery.startsWith(">")) tx.amount > amountFilter
                else tx.amount < amountFilter
            } else {
                tx.description.contains(trimmedQuery, ignoreCase = true) ||
                        tx.category.displayName.contains(trimmedQuery, ignoreCase = true) ||
                        tx.subCategoryName.contains(trimmedQuery, ignoreCase = true)
            }
        }
    }

    private fun List<Transaction>.filterByBank(bank: String) = filter {
        if (bank == "Всі") true else it.bankName == bank
    }

    private fun List<Transaction>.filterByType(type: String) = filter {
        when (type) {
            "Витрати" -> it.isExpense
            "Доходи" -> !it.isExpense
            else -> true
        }
    }

    private fun List<Transaction>.filterByCategories(selectedCats: Set<String>) = filter {
        if (selectedCats.isEmpty()) true
        else selectedCats.contains(it.category.displayName) || selectedCats.contains(it.subCategoryName)
    }

    private fun List<Transaction>.filterByDate(range: LongRange?) = filter {
        if (range == null) true else it.timestamp in range
    }

    private fun List<Transaction>.applySort(order: SortOrder) = when (order) {
        SortOrder.DATE_DESC -> sortedByDescending { it.timestamp }
        SortOrder.DATE_ASC -> sortedBy { it.timestamp }
        SortOrder.AMOUNT_DESC -> sortedByDescending { it.amount }
        SortOrder.AMOUNT_ASC -> sortedBy { it.amount }
    }
}