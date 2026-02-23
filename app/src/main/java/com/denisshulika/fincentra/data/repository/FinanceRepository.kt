package com.denisshulika.fincentra.data.repository

import android.util.Log
import com.denisshulika.fincentra.data.models.domain.*
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.data.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

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

    private fun startAllListeners(uid: String) {
        val userDoc = db.collection(FirestoreCollections.USERS).document(uid)

        acL?.remove()
        acL = userDoc.collection(FirestoreCollections.ACCOUNTS)
            .addSnapshotListener { s, _ ->
                _accounts.value = s?.toObjects(BankAccount::class.java) ?: emptyList()
                _isAccountsLoaded.value = true
            }

        txL?.remove()
        txL = userDoc.collection(FirestoreCollections.TRANSACTIONS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { s, _ ->
                if (s != null) _transactions.value = s.toObjects(Transaction::class.java).distinctBy { it.id }
                _isTransactionsLoaded.value = true
            }

        val cal = java.util.Calendar.getInstance()
        val monthYear = "${cal.get(java.util.Calendar.MONTH) + 1}-${cal.get(java.util.Calendar.YEAR)}"

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
        txL?.remove(); acL?.remove(); buL?.remove(); drL?.remove()
        _transactions.value = emptyList(); _accounts.value = emptyList()
        _budgets.value = emptyList(); _dream.value = null
        _isTransactionsLoaded.value = false; _isAccountsLoaded.value = false
        _isBudgetsLoaded.value = false; _isDreamLoaded.value = false
    }

    private fun getTransactionsRef() = auth.currentUser?.uid?.let { db.collection(FirestoreCollections.USERS).document(it).collection(FirestoreCollections.TRANSACTIONS) }
    suspend fun addTransaction(tx: Transaction) { getTransactionsRef()?.document(tx.id)?.set(tx) }
    suspend fun addTransactionsBatch(list: List<Transaction>) {
        val ref = getTransactionsRef() ?: return
        val batch = db.batch()
        list.forEach { batch.set(ref.document(it.id), it) }
        batch.commit()
    }
    suspend fun deleteTransaction(id: String) { getTransactionsRef()?.document(id)?.delete() }
    suspend fun getAccountsOnce() = auth.currentUser?.uid?.let { db.collection(FirestoreCollections.USERS).document(it).collection(FirestoreCollections.ACCOUNTS).get().await().toObjects(BankAccount::class.java) } ?: emptyList()
    suspend fun saveAccounts(accounts: List<BankAccount>, updateSelection: Boolean = false) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection(FirestoreCollections.USERS).document(uid).collection(FirestoreCollections.ACCOUNTS)
        val batch = db.batch()
        accounts.forEach { batch.set(ref.document(it.id), it, SetOptions.merge()) }
        batch.commit().await()
    }
    fun getAccountsFlow() = accounts
    suspend fun deleteMonobankAccounts() {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection(FirestoreCollections.USERS).document(uid).collection(FirestoreCollections.ACCOUNTS)
        try {
            val snapshot = ref.whereEqualTo("provider", BankProviders.MONOBANK).get().await()
            val batch = db.batch()
            for (document in snapshot.documents) { batch.delete(document.reference) }
            batch.commit().await()
        } catch (e: Exception) { }
    }
}