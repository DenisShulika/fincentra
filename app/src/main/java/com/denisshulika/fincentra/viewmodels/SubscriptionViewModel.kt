package com.denisshulika.fincentra.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denisshulika.fincentra.data.models.domain.SubFrequency
import com.denisshulika.fincentra.data.models.domain.Subscription
import com.denisshulika.fincentra.data.util.DateFormatter
import com.denisshulika.fincentra.di.DependencyProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class SubscriptionViewModel : ViewModel() {
    private val subRepo = DependencyProvider.subscriptionRepository

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val allSubscriptions = subRepo.getManualSubscriptions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingSubscriptions = allSubscriptions.map { list ->
        val now = System.currentTimeMillis()
        val weekOut = now + (7 * 24 * 60 * 60 * 1000)
        list.filter { it.nextPaymentDate in now..weekOut }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount = _amount.asStateFlow()

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate = _selectedDate.asStateFlow()

    private val _selectedCurrency = MutableStateFlow(980)
    val selectedCurrency = _selectedCurrency.asStateFlow()

    private val _selectedFrequency = MutableStateFlow(SubFrequency.MONTHLY)
    val selectedFrequency = _selectedFrequency.asStateFlow()

    private val _editingSubId = MutableStateFlow<String?>(null)
    val editingSubId = _editingSubId.asStateFlow()

    init {
        viewModelScope.launch {
            allSubscriptions.collect { subs ->
                checkAndAutoRenewSubscriptions(subs)
            }
        }
    }

    fun onNameChange(v: String) {
        _name.value = v
    }

    fun onAmountChange(v: String) {
        val sanitized = v.replace(',', '.')
        _amount.value = sanitized.filter { it.isDigit() || it == '.' }
    }

    fun onDateChange(v: Long) {
        _selectedDate.value = v
    }

    fun onCurrencyChange(code: Int) {
        _selectedCurrency.value = code
    }

    fun onFrequencyChange(freq: SubFrequency) {
        _selectedFrequency.value = freq
    }

    fun resetForm() {
        _name.value = ""
        _amount.value = ""
        _selectedDate.value = System.currentTimeMillis()
        _editingSubId.value = null
    }


    fun saveSubscription() {
        val amt = _amount.value.toDoubleOrNull() ?: return
        val currentId = _editingSubId.value ?: UUID.randomUUID().toString()

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val sub = Subscription(
                    id = currentId,
                    name = _name.value,
                    amount = amt,
                    nextPaymentDate = _selectedDate.value,
                    currencyCode = _selectedCurrency.value,
                    frequency = _selectedFrequency.value.name
                )
                subRepo.saveSubscription(sub)
                resetForm()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun checkAndAutoRenewSubscriptions(subs: List<Subscription>) {
        val now = System.currentTimeMillis()

        subs.forEach { sub ->
            if (sub.nextPaymentDate < now && sub.nextPaymentDate != 0L) {
                viewModelScope.launch {
                    val newDate = calculateNextDate(sub.nextPaymentDate, sub.frequency)
                    val updatedSub = sub.copy(nextPaymentDate = newDate)
                    subRepo.saveSubscription(updatedSub)
                    Log.d(
                        "SUB_SYNC",
                        "Subscription ${sub.name} renewed to ${DateFormatter.formatFullDate(newDate)}"
                    )
                }
            }
        }
    }

    private fun calculateNextDate(currentDate: Long, frequency: String): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = currentDate

        when (frequency) {
            "WEEKLY" -> cal.add(java.util.Calendar.WEEK_OF_YEAR, 1)
            "YEARLY" -> cal.add(java.util.Calendar.YEAR, 1)
            else -> cal.add(java.util.Calendar.MONTH, 1)
        }

        return if (cal.timeInMillis < System.currentTimeMillis()) {
            calculateNextDate(cal.timeInMillis, frequency)
        } else {
            cal.timeInMillis
        }
    }

    fun prepareForEdit(sub: Subscription) {
        _name.value = sub.name
        _amount.value = sub.amount.toString()
        _selectedDate.value = sub.nextPaymentDate
        _selectedCurrency.value = sub.currencyCode
        _selectedFrequency.value = try {
            SubFrequency.valueOf(sub.frequency)
        } catch (e: Exception) {
            SubFrequency.MONTHLY
        }
        _editingSubId.value = sub.id
    }

    fun deleteSubscription(id: String) {
        viewModelScope.launch { subRepo.deleteSubscription(id) }
    }
}