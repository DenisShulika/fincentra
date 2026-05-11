package com.denisshulika.fincentra.data.repository

import android.util.Log
import com.denisshulika.fincentra.data.models.domain.BankAccount
import com.denisshulika.fincentra.data.models.domain.Budget
import com.denisshulika.fincentra.data.models.domain.Dream
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.data.util.BankProviders
import com.denisshulika.fincentra.data.util.FirestoreCollections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
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
import kotlin.math.abs

class FinanceRepository(private val db: FirebaseFirestore, private val auth: FirebaseAuth) {
    private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _accounts = MutableStateFlow<List<BankAccount>>(emptyList())
    val accounts: StateFlow<List<BankAccount>> = _accounts.asStateFlow()

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets.asStateFlow()

    private val _dream = MutableStateFlow<Dream?>(null)
    val dream: StateFlow<Dream?> = _dream.asStateFlow()

    private var txL: ListenerRegistration? = null
    private var acL: ListenerRegistration? = null
    private var buL: ListenerRegistration? = null
    private var drL: ListenerRegistration? = null

    private val _isTransactionsLoaded = MutableStateFlow(false)
    private val _isAccountsLoaded = MutableStateFlow(false)
    private val _isBudgetsLoaded = MutableStateFlow(false)
    private val _isDreamLoaded = MutableStateFlow(false)

    val isInitialLoadComplete: StateFlow<Boolean> = combine(
        _isTransactionsLoaded, _isAccountsLoaded, _isBudgetsLoaded, _isDreamLoaded
    ) { tx, acc, budg, dr ->
        tx && acc && budg && dr
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                startAllListeners(uid)
            } else {
                clearAllData()
            }
        }
    }

    fun refreshUser() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            startAllListeners(uid)
        } else {
            clearAllData()
        }
    }

    private fun startAllListeners(uid: String) {
        _transactions.value = emptyList()
        _accounts.value = emptyList()
        _isTransactionsLoaded.value = false
        _isAccountsLoaded.value = false

        val userDoc = db.collection(FirestoreCollections.USERS).document(uid)

        txL?.remove()
        txL = userDoc.collection(FirestoreCollections.TRANSACTIONS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener(MetadataChanges.INCLUDE) { s, _ ->
                if (s != null) {
                    val serverItems = s.toObjects(Transaction::class.java)
                    val currentLocalItems = _transactions.value

                    val merged = (serverItems + currentLocalItems)
                        .distinctBy { it.id }
                        .sortedByDescending { it.timestamp }

                    _transactions.value = merged
                }
                _isTransactionsLoaded.value = true
            }

        acL?.remove()
        acL = userDoc.collection(FirestoreCollections.ACCOUNTS)
            .addSnapshotListener(MetadataChanges.INCLUDE) { s, _ ->
                if (s != null) {
                    val serverAccounts = s.toObjects(BankAccount::class.java)
                    val currentLocal = _accounts.value
                    val merged = (serverAccounts + currentLocal)
                        .distinctBy { it.id }

                    _accounts.value = merged
                }
                _isAccountsLoaded.value = true
            }

        val cal = java.util.Calendar.getInstance()
        val monthYear =
            "${cal.get(java.util.Calendar.MONTH) + 1}-${cal.get(java.util.Calendar.YEAR)}"

        buL?.remove()
        buL = userDoc.collection("budgets").whereEqualTo("monthYear", monthYear)
            .addSnapshotListener { s, _ ->
                _budgets.value = s?.toObjects(Budget::class.java) ?: emptyList()
                _isBudgetsLoaded.value = true
            }

        drL?.remove()
        drL = userDoc.collection(FirestoreCollections.SETTINGS).document("user_dream")
            .addSnapshotListener { s, _ ->
                _dream.value = s?.toObject(Dream::class.java)
                _isDreamLoaded.value = true
            }
    }

    fun clearAllData() {
        txL?.remove()
        acL?.remove()
        buL?.remove()
        drL?.remove()

        _transactions.value = emptyList()
        _accounts.value = emptyList()
        _budgets.value = emptyList()
        _dream.value = null

        _isTransactionsLoaded.value = false
        _isAccountsLoaded.value = false
        _isBudgetsLoaded.value = false
        _isDreamLoaded.value = false
    }

    private fun getTransactionsRef() = auth.currentUser?.uid?.let {
        db.collection(FirestoreCollections.USERS).document(it)
            .collection(FirestoreCollections.TRANSACTIONS)
    }

    fun addTransaction(tx: Transaction) {
        val ref = getTransactionsRef() ?: return

        if (tx.sourceType == "DIRECT") {
            val duplicate = _transactions.value.find {
                it.sourceType == "NOTIFICATION" &&
                        it.amount == tx.amount &&
                        abs(it.timestamp - tx.timestamp) < 600000
            }
            duplicate?.let { deleteTransaction(it.id) }
        }

        ref.document(tx.id).set(tx)
    }

    suspend fun addTransactionsBatch(list: List<Transaction>) {
        val ref = getTransactionsRef() ?: return

        val currentTxs = _transactions.value.toMutableList()
        currentTxs.addAll(0, list)
        _transactions.value = currentTxs.distinctBy { it.id }.sortedByDescending { it.timestamp }

        try {
            val batch = db.batch()
            list.forEach { batch.set(ref.document(it.id), it) }
            batch.commit().await()
        } catch (e: Exception) {
            Log.e("REPO", "Batch sync failed, but local data is kept: ${e.message}")
        }
    }

    suspend fun getAccountsOnce() = auth.currentUser?.uid?.let {
        db.collection(FirestoreCollections.USERS).document(it)
            .collection(FirestoreCollections.ACCOUNTS).get().await()
            .toObjects(BankAccount::class.java)
    } ?: emptyList()

    suspend fun saveAccounts(newAccounts: List<BankAccount>, updateSelection: Boolean = false) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection(FirestoreCollections.USERS).document(uid)
            .collection(FirestoreCollections.ACCOUNTS)

        val currentList = _accounts.value.toMutableList()
        newAccounts.forEach { newAcc ->
            val index = currentList.indexOfFirst { it.id == newAcc.id }
            if (index != -1) currentList[index] = newAcc else currentList.add(newAcc)
        }
        _accounts.value = currentList

        val batch = db.batch()
        newAccounts.forEach { batch.set(ref.document(it.id), it, SetOptions.merge()) }
        batch.commit().await()
    }

    fun deleteTransaction(id: String) {
        getTransactionsRef()?.document(id)?.delete()
    }

    fun getAccountsFlow() = accounts
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

    suspend fun deleteAccountsByProvider(providerId: String) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection(FirestoreCollections.USERS).document(uid)
            .collection(FirestoreCollections.ACCOUNTS)
        val snapshot = ref.whereEqualTo("provider", providerId).get().await()
        val batch = db.batch()
        for (doc in snapshot.documents) batch.delete(doc.reference)
        batch.commit().await()
        _accounts.value = _accounts.value.filter { it.provider != providerId }
    }
}