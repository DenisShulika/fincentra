package com.denisshulika.fincentra.data.repository

import android.util.Log
import com.denisshulika.fincentra.data.models.domain.BankAccount
import com.denisshulika.fincentra.data.models.domain.Budget
import com.denisshulika.fincentra.data.models.domain.Dream
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.data.util.BankProviders
import com.denisshulika.fincentra.data.util.FirestoreCollections
import com.denisshulika.fincentra.data.util.FirestoreDocuments
import com.denisshulika.fincentra.di.DependencyProvider
import com.google.firebase.auth.FirebaseAuth
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

class FinanceRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
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

    fun observeUserTransactions() {
        val ref = getTransactionsRef() ?: run {
            _isInitialLoadComplete.value = true
            return
        }

        transactionsListener?.remove()
        transactionsListener = ref
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _isInitialLoadComplete.value = true
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Transaction::class.java)
                    _transactions.value = list.distinctBy { it.id }
                    _isInitialLoadComplete.value = true
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

    private val _isInitialLoadComplete = MutableStateFlow(false)
    val isInitialLoadComplete: StateFlow<Boolean> = _isInitialLoadComplete.asStateFlow()

    suspend fun saveAccounts(accounts: List<BankAccount>, updateSelection: Boolean = false) {
        val ref = getAccountsRef() ?: return
        val batch = db.batch()
        accounts.forEach { acc ->
            val docRef = ref.document(acc.id)
            batch.set(docRef, acc, SetOptions.merge())
        }
        batch.commit().await()
    }

    suspend fun deleteMonobankAccounts() {
        val ref = getAccountsRef() ?: return
        val snapshot = ref.whereEqualTo("provider", BankProviders.MONOBANK).get().await()
        val batch = db.batch()
        snapshot.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }
}