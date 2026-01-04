package com.denisshulika.fincentra.data.repository

import android.util.Log
import com.denisshulika.fincentra.data.models.BankAccount
import com.denisshulika.fincentra.data.models.Transaction
import com.denisshulika.fincentra.data.util.BankProviders
import com.denisshulika.fincentra.data.util.FirestoreCollections
import com.denisshulika.fincentra.data.util.FirestoreDocuments
import com.denisshulika.fincentra.di.DependencyProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class FinanceRepository {

    private val db: FirebaseFirestore = DependencyProvider.getInstance()
    private val auth = DependencyProvider.auth

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private var transactionsListener: ListenerRegistration? = null
    private var accountsListener: ListenerRegistration? = null

    init {
        observeUserTransactions()
    }

    private fun getUserDoc(): DocumentReference? {
        val uid = auth.currentUser?.uid ?: return null
        return db.collection(FirestoreCollections.USERS).document(uid)
    }

    private fun getTransactionsRef(): CollectionReference? =
        getUserDoc()?.collection(FirestoreCollections.TRANSACTIONS)

    private fun getAccountsRef(): CollectionReference? =
        getUserDoc()?.collection(FirestoreCollections.ACCOUNTS)

    private fun getSettingsRef(): CollectionReference? =
        getUserDoc()?.collection(FirestoreCollections.SETTINGS)

    fun observeUserTransactions() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            clearAllData()
            return
        }

        transactionsListener?.remove()
        transactionsListener = getTransactionsRef()
            ?.orderBy("timestamp", Query.Direction.DESCENDING)
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("REPO", "Помилка слухача транзакцій", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Transaction::class.java)
                    _transactions.value = list.distinctBy { it.id }
                }
            }
    }

    fun clearAllData() {
        transactionsListener?.remove()
        accountsListener?.remove()
        transactionsListener = null
        accountsListener = null
        _transactions.value = emptyList()
    }

    suspend fun addTransaction(transaction: Transaction) {
        val ref = getTransactionsRef() ?: return
        try {
            ref.document(transaction.id).set(transaction).await()
        } catch (e: Exception) {
            Log.e("REPO", "Помилка addTransaction: ${e.message}")
            throw e
        }
    }

    suspend fun addTransactionsBatch(list: List<Transaction>) {
        val ref = getTransactionsRef() ?: return
        if (list.isEmpty()) return
        val batch = db.batch()
        list.forEach { batch.set(ref.document(it.id), it) }
        batch.commit().await()
    }

    suspend fun deleteTransaction(id: String) {
        getTransactionsRef()?.document(id)?.delete()?.await()
    }

    suspend fun getAccountsOnce(): List<BankAccount> {
        val ref = getAccountsRef() ?: return emptyList()
        return try {
            val snapshot = ref.get().await()
            snapshot.toObjects(BankAccount::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getAccountsFlow(): Flow<List<BankAccount>> {
        val ref = getAccountsRef() ?: return flowOf(emptyList())
        return callbackFlow {
            val subscription = ref.addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(BankAccount::class.java))
                }
            }
            awaitClose { subscription.remove() }
        }
    }

    suspend fun saveAccounts(accounts: List<BankAccount>, updateSelection: Boolean = false) {
        val ref = getAccountsRef() ?: return
        val selectedIds = getSelectedAccountIds()
        val batch = db.batch()

        accounts.forEach { acc ->
            val docRef = ref.document(acc.id)
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

    suspend fun saveMonoToken(token: String) {
        getSettingsRef()?.document(FirestoreDocuments.USER_SETTINGS)
            ?.set(mapOf("monoToken" to token), SetOptions.merge())
            ?.await()
    }

    suspend fun getMonoToken(): String? {
        return try {
            val doc = getSettingsRef()?.document(FirestoreDocuments.USER_SETTINGS)?.get()?.await()
            doc?.getString("monoToken")
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun saveSelectedAccountIds(ids: List<String>) {
        getSettingsRef()?.document(FirestoreDocuments.USER_SETTINGS)
            ?.set(mapOf("selectedIds" to ids), SetOptions.merge())
            ?.await()
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun getSelectedAccountIds(): List<String> {
        return try {
            val snapshot = getSettingsRef()?.document(FirestoreDocuments.USER_SETTINGS)?.get()?.await()
            snapshot?.get("selectedIds") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveLastSyncTimestamp(accountId: String, timestamp: Long) {
        getSettingsRef()?.document(FirestoreDocuments.SYNC_METADATA)
            ?.set(mapOf("lastSync_$accountId" to timestamp), SetOptions.merge())
            ?.await()
    }

    suspend fun getLastSyncTimestamp(accountId: String): Long {
        return try {
            val snapshot = getSettingsRef()?.document(FirestoreDocuments.SYNC_METADATA)?.get()?.await()
            snapshot?.getLong("lastSync_$accountId") ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    suspend fun saveLastGlobalSyncTime(timestamp: Long) {
        getSettingsRef()?.document(FirestoreDocuments.SYNC_METADATA)
            ?.set(mapOf("lastGlobalSync" to timestamp), SetOptions.merge())
            ?.await()
    }

    fun getLastGlobalSyncTimeFlow(): Flow<Long?> {
        val ref = getSettingsRef() ?: return flowOf(null)
        return callbackFlow {
            val subscription = ref.document(FirestoreDocuments.SYNC_METADATA)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        trySend(snapshot.getLong("lastGlobalSync"))
                    }
                }
            awaitClose { subscription.remove() }
        }
    }

    suspend fun clearMonobankData() {
        val settingsRef = getSettingsRef() ?: return
        val accountsRef = getAccountsRef() ?: return

        settingsRef.document(FirestoreDocuments.USER_SETTINGS).update(
            mapOf(
                "monoToken" to null,
                "selectedIds" to emptyList<String>()
            )
        ).await()

        val accounts = accountsRef.whereEqualTo("provider", BankProviders.MONOBANK).get().await()
        val batch = db.batch()
        accounts.documents.forEach { batch.delete(it.reference) }
        settingsRef.document(FirestoreDocuments.SYNC_METADATA).delete().await()
        batch.commit().await()
    }
}