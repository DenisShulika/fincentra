package com.denisshulika.fincentra.data.repository

import com.denisshulika.fincentra.data.models.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FinanceRepository {
    private val db = FirebaseFirestore.getInstance()
    private val transactionsCollection = db.collection("transactions")

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    suspend fun fetchTransactions() {
        try {
            val snapshot = transactionsCollection.get().await()
            val list = snapshot.toObjects(Transaction::class.java)
            _transactions.value = list
        } catch (e: Exception) {

        }
    }

    suspend fun addTransaction(transaction: Transaction) {
        transactionsCollection.document(transaction.id).set(transaction).await()
        _transactions.value = listOf(transaction) + _transactions.value
    }

    suspend fun deleteTransaction(transactionId: String) {
        transactionsCollection.document(transactionId).delete().await()
        _transactions.value = _transactions.value.filter { it.id != transactionId }
    }
}