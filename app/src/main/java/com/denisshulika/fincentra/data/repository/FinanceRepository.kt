package com.denisshulika.fincentra.data.repository

import android.util.Log
import com.denisshulika.fincentra.data.models.domain.BankAccount
import com.denisshulika.fincentra.data.models.domain.Budget
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.data.util.BankProviders
import com.denisshulika.fincentra.data.util.FirestoreCollections
import com.denisshulika.fincentra.data.util.FirestoreDocuments
import com.denisshulika.fincentra.di.DependencyProvider
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

    private val _accounts = MutableStateFlow<List<BankAccount>>(emptyList())
    val accounts: StateFlow<List<BankAccount>> = _accounts.asStateFlow()

    private var transactionsListener: ListenerRegistration? = null
    private var accountsListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                observeUserTransactions()
                observeUserAccounts()
            } else {
                clearAllData()
            }
        }
    }

    private fun getUserDoc(): DocumentReference? {
        val uid = auth.currentUser?.uid ?: return null
        return db.collection(FirestoreCollections.USERS).document(uid)
    }

    private fun getTransactionsRef() = getUserDoc()?.collection(FirestoreCollections.TRANSACTIONS)
    private fun getAccountsRef() = getUserDoc()?.collection(FirestoreCollections.ACCOUNTS)
    private fun getSettingsRef() = getUserDoc()?.collection(FirestoreCollections.SETTINGS)
    private fun getBudgetsRef() = getUserDoc()?.collection("budgets")

    fun observeUserTransactions() {
        transactionsListener?.remove()
        transactionsListener = getTransactionsRef()
            ?.orderBy("timestamp", Query.Direction.DESCENDING)
            ?.addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _transactions.value =
                        snapshot.toObjects(Transaction::class.java).distinctBy { it.id }
                }
            }
    }

    private fun observeUserAccounts() {
        accountsListener?.remove()
        accountsListener = getAccountsRef()
            ?.addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _accounts.value = snapshot.toObjects(BankAccount::class.java)
                }
            }
    }

    fun clearAllData() {
        transactionsListener?.remove()
        accountsListener?.remove()
        transactionsListener = null
        accountsListener = null

        _transactions.value = emptyList()
        _accounts.value = emptyList()
        Log.d("REPO", "Дані успішно очищено при виході")
    }

    fun getAccountsFlow(): Flow<List<BankAccount>> = accounts

    suspend fun addTransaction(transaction: Transaction) {
        getTransactionsRef()?.document(transaction.id)?.set(transaction)?.await()
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
        return try {
            val ref = getAccountsRef() ?: return emptyList()
            val snapshot = ref.get().await()
            snapshot?.toObjects(BankAccount::class.java) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
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

    suspend fun saveBudget(budget: Budget) {
        getBudgetsRef()?.document(budget.id)?.set(budget)?.await()
    }

    fun getBudgetsFlow(monthYear: String): Flow<List<Budget>> {
        val ref = getBudgetsRef() ?: return flowOf(emptyList())
        return callbackFlow {
            val subscription = ref
                .whereEqualTo("monthYear", monthYear)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        trySend(snapshot.toObjects(Budget::class.java))
                    }
                }
            awaitClose { subscription.remove() }
        }
    }

    suspend fun saveMonobankApiToken(token: String) {
        getSettingsRef()?.document(FirestoreDocuments.USER_SETTINGS)
            ?.set(mapOf("monobankToken" to token), SetOptions.merge())
            ?.await()
    }

    suspend fun getMonobankApiToken(): String? {
        return try {
            val doc = getSettingsRef()?.document(FirestoreDocuments.USER_SETTINGS)?.get()?.await()
            doc?.getString("monobankToken")
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun saveSelectedAccountIds(ids: List<String>) {
        getSettingsRef()?.document(FirestoreDocuments.USER_SETTINGS)
            ?.set(mapOf("selectedIds" to ids), SetOptions.merge())
            ?.await()
    }

    suspend fun getSelectedAccountIds(): List<String> {
        return try {
            val snapshot =
                getSettingsRef()?.document(FirestoreDocuments.USER_SETTINGS)?.get()?.await()
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
            val snapshot =
                getSettingsRef()?.document(FirestoreDocuments.SYNC_METADATA)?.get()?.await()
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

    suspend fun clearAllMonobankData() {
        val settingsRef = getSettingsRef() ?: return
        val accountsRef = getAccountsRef() ?: return

        settingsRef.document(FirestoreDocuments.USER_SETTINGS).update(
            mapOf("monobankToken" to null, "selectedIds" to emptyList<String>())
        ).await()

        val accounts = accountsRef.whereEqualTo("provider", BankProviders.MONOBANK).get().await()
        val batch = db.batch()
        accounts.documents.forEach { batch.delete(it.reference) }
        settingsRef.document(FirestoreDocuments.SYNC_METADATA).delete().await()
        batch.commit().await()
    }

    private var lastVisibleDocument: com.google.firebase.firestore.DocumentSnapshot? = null
    private val PAGE_SIZE = 20L

    suspend fun fetchNextPage(): List<Transaction> {
        val ref = getTransactionsRef() ?: return emptyList()
        val query = if (lastVisibleDocument == null) {
            ref.orderBy("timestamp", Query.Direction.DESCENDING).limit(PAGE_SIZE)
        } else {
            ref.orderBy("timestamp", Query.Direction.DESCENDING).startAfter(lastVisibleDocument!!)
                .limit(PAGE_SIZE)
        }
        return try {
            val snapshot = query.get().await()
            if (snapshot.documents.isNotEmpty()) lastVisibleDocument = snapshot.documents.last()
            snapshot.toObjects(Transaction::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun resetPagination() {
        lastVisibleDocument = null
    }

    suspend fun deleteBudget(budgetId: String) {
        val ref = getBudgetsRef() ?: return
        try {
            ref.document(budgetId).delete().await()
            Log.d("REPO", "Бюджет видалено: $budgetId")
        } catch (e: Exception) {
            Log.e("REPO", "Помилка видалення бюджету: ${e.message}")
        }
    }
}