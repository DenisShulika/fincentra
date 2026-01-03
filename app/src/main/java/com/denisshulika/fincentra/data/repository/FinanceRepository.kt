package com.denisshulika.fincentra.data.repository

import android.util.Log
import com.denisshulika.fincentra.data.models.BankAccount
import com.denisshulika.fincentra.data.models.Transaction
import com.denisshulika.fincentra.data.util.BankProviders
import com.denisshulika.fincentra.data.util.FirestoreCollections
import com.denisshulika.fincentra.data.util.FirestoreDocuments
import com.denisshulika.fincentra.di.DependencyProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FinanceRepository {

    private val db: FirebaseFirestore = DependencyProvider.getInstance()
    private val transactionsCollection = db.collection(FirestoreCollections.TRANSACTIONS)
    private val accountsCollection = db.collection(FirestoreCollections.ACCOUNTS)
    private val settingsCollection = db.collection(FirestoreCollections.SETTINGS)

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    init {
        transactionsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("REPO", "Слухач впав", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Transaction::class.java)
                    _transactions.value = list.distinctBy { it.id }
                }
            }
    }

    suspend fun addTransaction(transaction: Transaction) {
        try {
            transactionsCollection.document(transaction.id).set(transaction).await()
            Log.d("REPO", "Транзакція збережена успішно: ${transaction.id}")
        } catch (e: Exception) {
            Log.e("REPO", "Помилка збереження однієї транзакції: ${e.message}")
            throw e
        }
    }

    suspend fun getAccountsOnce(): List<BankAccount> {
        return try {
            val snapshot = accountsCollection.get().await()
            snapshot.toObjects(BankAccount::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveAccounts(accounts: List<BankAccount>, updateSelection: Boolean = false) {
        val selectedIds = getSelectedAccountIds()
        val batch = db.batch()
        accounts.forEach { acc ->
            val docRef = accountsCollection.document(acc.id)
            val isSelected = if (updateSelection) acc.selected else selectedIds.contains(acc.id)
            val updatedAcc = acc.copy(selected = isSelected)
            batch.set(docRef, updatedAcc, SetOptions.merge())
        }
        batch.commit().await()

        val newSelectedIds = accounts.filter { if (updateSelection) it.selected else selectedIds.contains(it.id) }.map { it.id }
        saveSelectedAccountIds(newSelectedIds)
    }

    suspend fun addTransactionsBatch(list: List<Transaction>) {
        if (list.isEmpty()) return
        val batch = db.batch()
        list.forEach { batch.set(transactionsCollection.document(it.id), it) }
        batch.commit().await()
    }

    suspend fun saveMonoToken(token: String) {
        settingsCollection.document(FirestoreDocuments.USER_SETTINGS)
            .set(mapOf("monoToken" to token), SetOptions.merge())
            .await()
    }

    suspend fun getMonoToken(): String? {
        return settingsCollection.document(FirestoreDocuments.USER_SETTINGS).get().await()
            .getString("monoToken")
    }

    private suspend fun saveSelectedAccountIds(ids: List<String>) {
        settingsCollection.document(FirestoreDocuments.USER_SETTINGS)
            .set(mapOf("selectedIds" to ids), SetOptions.merge())
            .await()
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun getSelectedAccountIds(): List<String> {
        val snapshot = settingsCollection.document(FirestoreDocuments.USER_SETTINGS).get().await()
        return snapshot.get("selectedIds") as? List<String> ?: emptyList()
    }

    suspend fun saveLastSyncTimestamp(accountId: String, timestamp: Long) {
        settingsCollection.document(FirestoreDocuments.SYNC_METADATA)
            .set(mapOf("lastSync_$accountId" to timestamp), SetOptions.merge())
            .await()
    }

    suspend fun getLastSyncTimestamp(accountId: String): Long {
        val snapshot = settingsCollection.document(FirestoreDocuments.SYNC_METADATA).get().await()
        return snapshot.getLong("lastSync_$accountId") ?: 0L
    }

    suspend fun saveLastGlobalSyncTime(timestamp: Long) {
        settingsCollection.document(FirestoreDocuments.SYNC_METADATA)
            .set(mapOf("lastGlobalSync" to timestamp), SetOptions.merge())
            .await()
    }

    fun getLastGlobalSyncTimeFlow(): Flow<Long?> = callbackFlow {
        val subscription = settingsCollection.document(FirestoreDocuments.SYNC_METADATA)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.getLong("lastGlobalSync"))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun deleteTransaction(id: String) {
        transactionsCollection.document(id).delete().await()
    }

    fun getAccountsFlow(): Flow<List<BankAccount>> = callbackFlow {
        val subscription = accountsCollection.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                trySend(snapshot.toObjects(BankAccount::class.java))
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun clearMonobankData() {
        settingsCollection.document(FirestoreDocuments.USER_SETTINGS).update(
            mapOf(
                "monoToken" to null,
                "selectedIds" to emptyList<String>()
            )
        ).await()

        val accounts = accountsCollection.whereEqualTo("provider", BankProviders.MONOBANK).get().await()
        //val transactions = transactionsCollection.whereEqualTo("bankName", "Monobank").get().await()

        val batch = db.batch()
        accounts.documents.forEach { batch.delete(it.reference) }
        //transactions.documents.forEach { batch.delete(it.reference) }

        settingsCollection.document(FirestoreDocuments.SYNC_METADATA).delete().await()

        batch.commit().await()
    }
}