package com.denisshulika.fincentra.data.repository

import com.denisshulika.fincentra.data.models.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FinanceRepository {
    private val db = FirebaseFirestore.getInstance()
    private val transactionsCollection = db.collection("transactions")

    suspend fun getAllTransactions(): List<Transaction> {
        return try {
            transactionsCollection.get().await().toObjects(Transaction::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addTransaction(transaction: Transaction) {
        transactionsCollection.document(transaction.id).set(transaction).await()
    }
}