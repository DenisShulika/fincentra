package com.denisshulika.fincentra.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.data.util.ExportManager
import com.denisshulika.fincentra.data.util.FilterConstants
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.flow.*

class ExportViewModel : ViewModel() {
    private val financeRepository = DependencyProvider.financeRepository

    private val _selectedDateRange = MutableStateFlow<LongRange?>(null)
    val selectedDateRange = _selectedDateRange.asStateFlow()

    private val _selectedType = MutableStateFlow(FilterConstants.ALL)
    val selectedType = _selectedType.asStateFlow()

    private val _selectedSources = MutableStateFlow<Set<String>>(emptySet())
    val selectedSources = _selectedSources.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories = _selectedCategories.asStateFlow()

    private val _includeHeader = MutableStateFlow(true)
    val includeHeader = _includeHeader.asStateFlow()

    private val _includeSummary = MutableStateFlow(true)
    val includeSummary = _includeSummary.asStateFlow()

    private val _selectedAccountIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedAccountIds = _selectedAccountIds.asStateFlow()

    private val _selectedCurrencies = MutableStateFlow<Set<Int>>(emptySet())
    val selectedCurrencies = _selectedCurrencies.asStateFlow()

    val availableSources: StateFlow<List<String>> = financeRepository.transactions
        .map { txs ->
            txs.map { it.bankName }.distinct().sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableAccounts = financeRepository.accounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        financeRepository.transactions,
        _selectedDateRange,
        _selectedType,
        _selectedSources,
        _selectedAccountIds,
        _selectedCurrencies
    ) { txs, range, type, sources, accIds, currencies ->
        txs.filter { tx ->
            val matchesDate = range == null || tx.timestamp in range
            val matchesType = when (type) {
                FilterConstants.EXPENSES -> tx.isExpense
                FilterConstants.INCOME -> !tx.isExpense
                else -> true
            }
            val matchesSource = sources.isEmpty() || sources.contains(tx.bankName)
            val matchesAccount = accIds.isEmpty() || accIds.contains(tx.accountId)

            val matchesCurrency = currencies.isEmpty() || currencies.contains(tx.currencyCode)

            matchesDate && matchesType && matchesSource && matchesAccount && matchesCurrency
        }.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCurrencies: StateFlow<List<Int>> = financeRepository.transactions
        .map { txs -> txs.map { it.currencyCode }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleCurrency(code: Int) {
        val current = _selectedCurrencies.value.toMutableSet()
        if (current.contains(code)) current.remove(code) else current.add(code)
        _selectedCurrencies.value = current
    }

    fun clearCurrencies() {
        _selectedCurrencies.value = emptySet()
    }

    fun clearAccountIds() {
        _selectedAccountIds.value = emptySet()
    }

    fun toggleAccountId(id: String) {
        val current = _selectedAccountIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _selectedAccountIds.value = current
    }

    fun setIncludeHeader(v: Boolean) {
        _includeHeader.value = v
    }

    fun setIncludeSummary(v: Boolean) {
        _includeSummary.value = v
    }

    fun setDateRange(range: LongRange?) {
        _selectedDateRange.value = range
    }

    fun setType(type: String) {
        _selectedType.value = type
    }

    fun toggleSource(source: String) {
        val current = _selectedSources.value.toMutableSet()
        if (current.contains(source)) current.remove(source) else current.add(source)
        _selectedSources.value = current
    }

    fun shareFile(context: Context, isPdf: Boolean) {
        val transactions = filteredTransactions.value
        if (transactions.isEmpty()) return

        val uri: Uri? = if (isPdf) {
            ExportManager.generatePdf(
                context,
                transactions,
                _includeHeader.value,
                _includeSummary.value
            )
        } else {
            ExportManager.generateCsv(
                context,
                transactions,
                _includeSummary.value
            )
        }

        if (uri != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (isPdf) "application/pdf" else "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Report via"))
        } else {
            Log.e("EXPORT_DEBUG", "Failed to get URI from ExportManager")
        }
    }

    fun toggleCategory(catName: String) {
        val current = _selectedCategories.value.toMutableSet()
        if (current.contains(catName)) current.remove(catName) else current.add(catName)
        _selectedCategories.value = current
    }
}