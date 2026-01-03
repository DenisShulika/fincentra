package com.denisshulika.fincentra.data.repository

import android.util.Log
import com.denisshulika.fincentra.data.models.BankAccount
import com.denisshulika.fincentra.data.models.Transaction
import com.denisshulika.fincentra.data.util.BankProviders
import com.denisshulika.fincentra.data.util.FirestoreCollections
import com.denisshulika.fincentra.data.util.FirestoreDocuments
import com.denisshulika.fincentra.di.DependencyProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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
    private val auth = DependencyProvider.auth

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private var transactionsListener: ListenerRegistration? = null

    init {
        observeUserTransactions()
    }

    private fun getUserDoc() = db.collection(FirestoreCollections.USERS)
        .document(auth.currentUser?.uid ?: "anonymous")

    private fun getTransactionsRef() = getUserDoc().collection(FirestoreCollections.TRANSACTIONS)
    private fun getAccountsRef() = getUserDoc().collection(FirestoreCollections.ACCOUNTS)
    private fun getSettingsRef() = getUserDoc().collection(FirestoreCollections.SETTINGS)

    fun observeUserTransactions() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _transactions.value = emptyList()
            return
        }

        transactionsListener?.remove()

        transactionsListener = getTransactionsRef()
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("REPO", "Слухач транзакцій впав", error)
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
            getTransactionsRef().document(transaction.id).set(transaction).await()
            Log.d("REPO", "Транзакція збережена успішно: ${transaction.id}")
        } catch (e: Exception) {
            Log.e("REPO", "Помилка збереження транзакції: ${e.message}")
            throw e
        }
    }

    suspend fun getAccountsOnce(): List<BankAccount> {
        return try {
            val snapshot = getAccountsRef().get().await()
            snapshot.toObjects(BankAccount::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveAccounts(accounts: List<BankAccount>, updateSelection: Boolean = false) {
        val selectedIds = getSelectedAccountIds()
        val batch = db.batch()
        accounts.forEach { acc ->
            val docRef = getAccountsRef().document(acc.id)
            val isSelected = if (updateSelection) acc.selected else selectedIds.contains(acc.id)
            val updatedAcc = acc.copy(selected = isSelected)
            batch.set(docRef, updatedAcc, SetOptions.merge())
        }
        batch.commit().await()

        val newSelectedIds = accounts
            .filter { if (updateSelection) it.selected else selectedIds.contains(it.id) }
            .map { it.id }
        saveSelectedAccountIds(newSelectedIds)
    }

    suspend fun addTransactionsBatch(list: List<Transaction>) {
        if (list.isEmpty()) return
        val batch = db.batch()
        list.forEach { batch.set(getTransactionsRef().document(it.id), it) }
        batch.commit().await()
    }

    suspend fun saveMonoToken(token: String) {
        getSettingsRef().document(FirestoreDocuments.USER_SETTINGS)
            .set(mapOf("monoToken" to token), SetOptions.merge())
            .await()
    }

    suspend fun getMonoToken(): String? {
        return try {
            val doc = getSettingsRef().document(FirestoreDocuments.USER_SETTINGS).get().await()
            doc.getString("monoToken")
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun saveSelectedAccountIds(ids: List<String>) {
        getSettingsRef().document(FirestoreDocuments.USER_SETTINGS)
            .set(mapOf("selectedIds" to ids), SetOptions.merge())
            .await()
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun getSelectedAccountIds(): List<String> {
        val snapshot = getSettingsRef().document(FirestoreDocuments.USER_SETTINGS).get().await()
        return snapshot.get("selectedIds") as? List<String> ?: emptyList()
    }

    suspend fun saveLastSyncTimestamp(accountId: String, timestamp: Long) {
        getSettingsRef().document(FirestoreDocuments.SYNC_METADATA)
            .set(mapOf("lastSync_$accountId" to timestamp), SetOptions.merge())
            .await()
    }

    suspend fun getLastSyncTimestamp(accountId: String): Long {
        val snapshot = getSettingsRef().document(FirestoreDocuments.SYNC_METADATA).get().await()
        return snapshot.getLong("lastSync_$accountId") ?: 0L
    }

    suspend fun saveLastGlobalSyncTime(timestamp: Long) {
        getSettingsRef().document(FirestoreDocuments.SYNC_METADATA)
            .set(mapOf("lastGlobalSync" to timestamp), SetOptions.merge())
            .await()
    }

    fun getLastGlobalSyncTimeFlow(): Flow<Long?> = callbackFlow {
        val subscription = getSettingsRef().document(FirestoreDocuments.SYNC_METADATA)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.getLong("lastGlobalSync"))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun deleteTransaction(id: String) {
        getTransactionsRef().document(id).delete().await()
    }

    fun getAccountsFlow(): Flow<List<BankAccount>> = callbackFlow {
        val subscription = getAccountsRef().addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                trySend(snapshot.toObjects(BankAccount::class.java))
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun clearMonobankData() {
        getSettingsRef().document(FirestoreDocuments.USER_SETTINGS).update(
            mapOf(
                "monoToken" to null,
                "selectedIds" to emptyList<String>()
            )
        ).await()

        val accounts = getAccountsRef().whereEqualTo("provider", BankProviders.MONOBANK).get().await()
        val batch = db.batch()
        accounts.documents.forEach { batch.delete(it.reference) }
        getSettingsRef().document(FirestoreDocuments.SYNC_METADATA).delete().await()
        batch.commit().await()
    }
}