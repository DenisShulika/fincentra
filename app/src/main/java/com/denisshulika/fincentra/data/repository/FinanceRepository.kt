package com.denisshulika.fincentra.data.repository

import com.denisshulika.fincentra.data.models.Transaction
import com.denisshulika.fincentra.di.DependencyProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FinanceRepository {
    private val db = DependencyProvider.getInstance()
    private val transactionsCollection = db.collection("transactions")

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    init {
        transactionsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    android.util.Log.e("REPO", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.toObjects(Transaction::class.java)
                    _transactions.value = list
                }
            }
    }

    fun fetchTransactions() {

    }

    suspend fun addTransaction(transaction: Transaction) {
        transactionsCollection.document(transaction.id).set(transaction).await()
    }

    suspend fun deleteTransaction(transactionId: String) {
        transactionsCollection.document(transactionId).delete().await()
    }

    private val settingsCollection = db.collection("settings")

    suspend fun saveMonoToken(token: String) {
        val data = mapOf("monoToken" to token)
        settingsCollection.document("user_settings").set(data).await()
    }

    suspend fun getMonoToken(): String? {
        return try {
            val document = settingsCollection.document("user_settings").get().await()
            document.getString("monoToken")
        } catch (e: Exception) {
            null
        }
    }
}