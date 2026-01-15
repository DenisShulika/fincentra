package com.denisshulika.fincentra.data.repository

import com.denisshulika.fincentra.data.models.domain.Budget
import com.denisshulika.fincentra.data.util.FirestoreCollections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BudgetRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun getBudgetsRef() = auth.currentUser?.uid?.let { uid ->
        db.collection(FirestoreCollections.USERS).document(uid).collection("budgets")
    }

    suspend fun saveBudget(budget: Budget) {
        getBudgetsRef()?.document(budget.id)?.set(budget)?.await()
    }

    fun getBudgetsFlow(monthYear: String): Flow<List<Budget>> {
        val ref = getBudgetsRef()
        return callbackFlow {
            val subscription = ref?.whereEqualTo("monthYear", monthYear)
                ?.addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        trySend(snapshot.toObjects(Budget::class.java))
                    }
                }
            awaitClose { subscription?.remove() }
        }
    }

    suspend fun deleteBudget(budgetId: String) {
        getBudgetsRef()?.document(budgetId)?.delete()?.await()
    }
}