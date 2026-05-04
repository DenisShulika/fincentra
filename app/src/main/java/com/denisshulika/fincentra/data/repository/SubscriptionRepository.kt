package com.denisshulika.fincentra.data.repository

import com.denisshulika.fincentra.data.models.domain.Subscription
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SubscriptionRepository(private val db: FirebaseFirestore, private val auth: FirebaseAuth) {
    private fun getRef() = auth.currentUser?.uid?.let {
        db.collection("users").document(it).collection("subscriptions")
    }

    fun getManualSubscriptions(): Flow<List<Subscription>> = callbackFlow {
        val sub = getRef()?.addSnapshotListener { s, _ ->
            trySend(s?.toObjects(Subscription::class.java) ?: emptyList())
        }
        awaitClose { sub?.remove() }
    }

    suspend fun saveSubscription(subscription: Subscription) {
        val ref = getRef() ?: return
        ref.document(subscription.id).set(subscription, SetOptions.merge()).await()
    }

    suspend fun deleteSubscription(id: String) {
        getRef()?.document(id)?.delete()?.await()
    }
}