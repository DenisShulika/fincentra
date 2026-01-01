package com.denisshulika.fincentra.data.repository

import android.util.Log
import com.denisshulika.fincentra.data.models.BankAccount
import com.denisshulika.fincentra.data.models.Transaction
import com.denisshulika.fincentra.di.DependencyProvider
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FinanceRepository {
    private val db = DependencyProvider.getInstance()
    private val transactionsCollection = db.collection("transactions")
    private val accountsCollection = db.collection("accounts")

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    init {
        transactionsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    _transactions.value = snapshot.toObjects(Transaction::class.java).distinctBy { it.id }
                }
            }
    }

    suspend fun addTransaction(transaction: Transaction) {
        try {
            transactionsCollection.document(transaction.id).set(transaction).await()
        } catch (e: Exception) {
            Log.e("REPO", "Помилка додавання: ${e.message}")
        }
    }

    suspend fun saveAccounts(actualAccounts: List<BankAccount>) {
        val selectedIds = getSelectedAccountIds()
        val batch = db.batch()
        actualAccounts.forEach { account ->
            val updated = account.copy(isSelected = selectedIds.contains(account.id))
            batch.set(accountsCollection.document(account.id), updated)
        }
        batch.commit().await()
    }

    fun getAccountsFlow(): kotlinx.coroutines.flow.Flow<List<BankAccount>> = callbackFlow {
        val sub = accountsCollection.addSnapshotListener { s, _ ->
            if (s != null) trySend(s.toObjects(BankAccount::class.java))
        }
        awaitClose { sub.remove() }
    }

    suspend fun addTransactionsBatch(list: List<Transaction>) {
        if (list.isEmpty()) return
        val batch = db.batch()
        list.forEach { batch.set(transactionsCollection.document(it.id), it) }
        batch.commit().await()
        Log.d("REPO", "Записано в базу: ${list.size} транзакцій")
    }

    suspend fun     deleteTransaction(id: String) = transactionsCollection.document(id).delete().await()

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

    suspend fun saveSelectedAccountIds(ids: List<String>) {
        val data = mapOf("selectedIds" to ids)
        settingsCollection.document("user_settings")
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    suspend fun getSelectedAccountIds(): List<String> {
        return try {
            val document = settingsCollection.document("user_settings").get().await()
            @Suppress("UNCHECKED_CAST")
            document.get("selectedIds") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveLastGlobalSyncTime(timestamp: Long) {
        val data = mapOf("lastGlobalSync" to timestamp)
        settingsCollection.document("sync_metadata")
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    fun getLastGlobalSyncTimeFlow(): kotlinx.coroutines.flow.Flow<Long?> = callbackFlow {
        val subscription = settingsCollection.document("sync_metadata")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.getLong("lastGlobalSync"))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveLastSyncTimestamp(accountId: String, timestamp: Long) {
        val data = mapOf("lastSync_$accountId" to timestamp)
        settingsCollection.document("sync_metadata")
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    suspend fun getLastSyncTimestamp(accountId: String): Long {
        return try {
            val document = settingsCollection.document("sync_metadata").get().await()
            document.getLong("lastSync_$accountId") ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}