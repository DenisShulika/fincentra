package com.denisshulika.fincentra.data.repository

import android.util.Log
import com.denisshulika.fincentra.data.models.domain.BankAccount
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.data.util.BankProviders
import com.denisshulika.fincentra.data.util.FirestoreCollections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FinanceRepository(private val db: FirebaseFirestore, private val auth: FirebaseAuth) {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _accounts = MutableStateFlow<List<BankAccount>>(emptyList())
    val accounts: StateFlow<List<BankAccount>> = _accounts.asStateFlow()

    private var transactionsListener: ListenerRegistration? = null
    private var accountsListener: ListenerRegistration? = null

    private val _isInitialLoadComplete = MutableStateFlow(false)
    val isInitialLoadComplete: StateFlow<Boolean> = _isInitialLoadComplete.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                observeUserAccounts(user.uid)
                observeUserTransactions()
            } else {
                clearAllData()
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

    private val _statsTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val statsTransactions: StateFlow<List<Transaction>> = _statsTransactions.asStateFlow()

    private var statsListener: ListenerRegistration? = null

    fun observeTransactionsForStats(query: com.denisshulika.fincentra.data.models.state.TransactionQuery) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection(FirestoreCollections.USERS)
            .document(uid)
            .collection(FirestoreCollections.TRANSACTIONS)

        var firestoreQuery: Query =
            ref.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)

        if (query.bank != "Всі") {
            firestoreQuery = firestoreQuery.whereEqualTo("bankName", query.bank)
        }

        query.dateRange?.let {
            firestoreQuery = firestoreQuery
                .whereGreaterThanOrEqualTo("timestamp", it.first)
                .whereLessThanOrEqualTo("timestamp", it.last)
        }

        statsListener?.remove()
        statsListener = firestoreQuery.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                _statsTransactions.value = snapshot.toObjects(Transaction::class.java)
            }
        }
    }

    suspend fun saveAccounts(accounts: List<BankAccount>, updateSelection: Boolean = false) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection(FirestoreCollections.USERS).document(uid)
            .collection(FirestoreCollections.ACCOUNTS)
        val batch = db.batch()
        accounts.forEach { acc ->
            batch.set(ref.document(acc.id), acc, com.google.firebase.firestore.SetOptions.merge())
        }
        batch.commit().await()
    }

    private fun observeUserAccounts(uid: String) {
        accountsListener?.remove()
        accountsListener = db.collection(FirestoreCollections.USERS).document(uid)
            .collection(FirestoreCollections.ACCOUNTS)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                val list = snapshot?.toObjects(BankAccount::class.java) ?: emptyList()
                _accounts.value = list
            }
    }

    suspend fun deleteMonobankAccounts() {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection(FirestoreCollections.USERS).document(uid)
            .collection(FirestoreCollections.ACCOUNTS)

        try {
            val snapshot = ref.whereEqualTo("provider", BankProviders.MONOBANK).get().await()
            val batch = db.batch()

            for (document in snapshot.documents) {
                batch.delete(document.reference)
            }

            batch.commit().await()
        } catch (e: Exception) {
        }
    }

    fun observeUserTransactions() {
        val uid = auth.currentUser?.uid ?: return

        transactionsListener?.remove()
        transactionsListener = db.collection(FirestoreCollections.USERS).document(uid)
            .collection(FirestoreCollections.TRANSACTIONS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
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

    fun clearAllData() {
        transactionsListener?.remove()
        accountsListener?.remove()
        _transactions.value = emptyList()
        _accounts.value = emptyList()
        _isInitialLoadComplete.value = false
    }

    private fun getTransactionsRef() = auth.currentUser?.uid?.let {
        db.collection(FirestoreCollections.USERS).document(it)
            .collection(FirestoreCollections.TRANSACTIONS)
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