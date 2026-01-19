package com.denisshulika.fincentra.data.repository

import android.util.Log
import com.denisshulika.fincentra.data.models.domain.BankAccount
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.data.models.state.TransactionQuery
import com.denisshulika.fincentra.data.util.BankProviders
import com.denisshulika.fincentra.data.util.FirestoreCollections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await

class FinanceRepository(private val db: FirebaseFirestore, private val auth: FirebaseAuth) {
    private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _statsTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val statsTransactions: StateFlow<List<Transaction>> = _statsTransactions.asStateFlow()

    private val _accounts = MutableStateFlow<List<BankAccount>>(emptyList())
    val accounts: StateFlow<List<BankAccount>> = _accounts.asStateFlow()

    private var transactionsListener: ListenerRegistration? = null
    private var statsListener: ListenerRegistration? = null
    private var accountsListener: ListenerRegistration? = null

    private val _isTransactionsLoaded = MutableStateFlow(false)
    private val _isAccountsLoaded = MutableStateFlow(false)
    private val _isStatsLoaded = MutableStateFlow(false)

    val isInitialLoadComplete: StateFlow<Boolean> = combine(
        _isTransactionsLoaded,
        _isAccountsLoaded,
        _isStatsLoaded
    ) { tx, acc, stats ->
        tx && acc && stats
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                observeUserAccounts(user.uid)
                observeUserTransactions(user.uid)
            } else {
                clearAllData()
            }
        }
    }

    private fun observeUserAccounts(uid: String) {
        accountsListener?.remove()
        accountsListener = db.collection(FirestoreCollections.USERS).document(uid)
            .collection(FirestoreCollections.ACCOUNTS)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _isAccountsLoaded.value = true
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(BankAccount::class.java) ?: emptyList()
                _accounts.value = list
                _isAccountsLoaded.value = true
            }
    }

    fun observeUserTransactions(uid: String = auth.currentUser?.uid ?: "") {
        if (uid.isBlank()) return
        transactionsListener?.remove()
        transactionsListener = db.collection(FirestoreCollections.USERS).document(uid)
            .collection(FirestoreCollections.TRANSACTIONS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _isTransactionsLoaded.value = true
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Transaction::class.java)
                    _transactions.value = list.distinctBy { it.id }
                    _isTransactionsLoaded.value = true
                }
            }
    }

    fun observeTransactionsForStats(query: TransactionQuery) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection(FirestoreCollections.USERS).document(uid)
            .collection(FirestoreCollections.TRANSACTIONS)

        var firestoreQuery: Query = ref.orderBy("timestamp", Query.Direction.ASCENDING)

        if (query.bank != "Всі") {
            firestoreQuery = firestoreQuery.whereEqualTo("bankName", query.bank)
        }

        query.dateRange?.let {
            firestoreQuery = firestoreQuery
                .whereGreaterThanOrEqualTo("timestamp", it.first)
                .whereLessThanOrEqualTo("timestamp", it.last)
        }

        statsListener?.remove()
        statsListener = firestoreQuery.addSnapshotListener { snapshot, e ->
            if (e != null) {
                _isStatsLoaded.value = true
                return@addSnapshotListener
            }
            if (snapshot != null) {
                _statsTransactions.value = snapshot.toObjects(Transaction::class.java)
                _isStatsLoaded.value = true
            }
        }
    }

    suspend fun getAccountsOnce(): List<BankAccount> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            db.collection(FirestoreCollections.USERS).document(uid)
                .collection(FirestoreCollections.ACCOUNTS)
                .get()
                .await()
                .toObjects(BankAccount::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveAccounts(accounts: List<BankAccount>, updateSelection: Boolean = false) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection(FirestoreCollections.USERS).document(uid).collection(FirestoreCollections.ACCOUNTS)
        val batch = db.batch()
        accounts.forEach { acc ->
            batch.set(ref.document(acc.id), acc, SetOptions.merge())
        }
        batch.commit().await()
    }

    suspend fun deleteMonobankAccounts() {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection(FirestoreCollections.USERS).document(uid).collection(FirestoreCollections.ACCOUNTS)
        try {
            val snapshot = ref.whereEqualTo("provider", BankProviders.MONOBANK).get().await()
            val batch = db.batch()
            for (document in snapshot.documents) {
                batch.delete(document.reference)
            }
            batch.commit().await()
        } catch (e: Exception) { }
    }

    fun clearAllData() {
        transactionsListener?.remove()
        statsListener?.remove()
        accountsListener?.remove()

        transactionsListener = null
        statsListener = null
        accountsListener = null

        _transactions.value = emptyList()
        _statsTransactions.value = emptyList()
        _accounts.value = emptyList()

        _isTransactionsLoaded.value = false
        _isAccountsLoaded.value = false
        _isStatsLoaded.value = false
    }

    private fun getTransactionsRef() = auth.currentUser?.uid?.let {
        db.collection(FirestoreCollections.USERS).document(it).collection(FirestoreCollections.TRANSACTIONS)
    }

    fun addTransaction(tx: Transaction) {
        getTransactionsRef()?.document(tx.id)?.set(tx)
    }

    fun addTransactionsBatch(list: List<Transaction>) {
        val ref = getTransactionsRef() ?: return
        val batch = db.batch()
        list.forEach { batch.set(ref.document(it.id), it) }
        batch.commit()
    }

    fun deleteTransaction(id: String) {
        getTransactionsRef()?.document(id)?.delete()
    }

    fun getAccountsFlow() = accounts
}