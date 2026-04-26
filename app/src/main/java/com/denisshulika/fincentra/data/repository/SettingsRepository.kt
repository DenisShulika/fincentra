package com.denisshulika.fincentra.data.repository

import com.denisshulika.fincentra.data.models.domain.Dream
import com.denisshulika.fincentra.data.util.FirestoreCollections
import com.denisshulika.fincentra.data.util.FirestoreDocuments
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class SettingsRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun getSettingsRef(): DocumentReference? {
        return auth.currentUser?.uid?.let { uid ->
            db.collection(FirestoreCollections.USERS).document(uid)
                .collection(FirestoreCollections.SETTINGS)
                .document(FirestoreDocuments.USER_SETTINGS)
        }
    }

    private fun getSyncMetadataRef(): DocumentReference? {
        return auth.currentUser?.uid?.let { uid ->
            db.collection(FirestoreCollections.USERS).document(uid)
                .collection(FirestoreCollections.SETTINGS)
                .document(FirestoreDocuments.SYNC_METADATA)
        }
    }

    fun getSelectedAccountIdsFlow(): Flow<List<String>> {
        val ref = getSettingsRef() ?: return flowOf(emptyList())
        return callbackFlow {
            val subscription = ref.addSnapshotListener { snapshot, _ ->
                val ids = snapshot?.get("selectedIds") as? List<String> ?: emptyList()
                trySend(ids)
            }
            awaitClose { subscription.remove() }
        }
    }

    suspend fun saveMonobankApiToken(token: String?) {
        getSettingsRef()?.set(mapOf("monobankToken" to token), SetOptions.merge())?.await()
    }

    suspend fun getMonobankApiToken(): String? {
        return getSettingsRef()?.get()?.await()?.getString("monobankToken")
    }

    suspend fun saveSelectedAccountIds(ids: List<String>) {
        getSettingsRef()?.set(mapOf("selectedIds" to ids), SetOptions.merge())?.await()
    }

    suspend fun getSelectedAccountIds(): List<String> {
        return getSettingsRef()?.get()?.await()?.get("selectedIds") as? List<String> ?: emptyList()
    }

    suspend fun saveLastSyncTimestamp(accountId: String, timestamp: Long) {
        getSyncMetadataRef()?.set(mapOf("lastSync_$accountId" to timestamp), SetOptions.merge())
            ?.await()
    }

    suspend fun getLastSyncTimestamp(accountId: String): Long {
        return getSyncMetadataRef()?.get()?.await()?.getLong("lastSync_$accountId") ?: 0L
    }

    suspend fun saveLastGlobalSyncTime(timestamp: Long) {
        getSyncMetadataRef()?.set(mapOf("lastGlobalSync" to timestamp), SetOptions.merge())?.await()
    }

    fun getLastGlobalSyncTimeFlow(): Flow<Long?> {
        val ref = getSyncMetadataRef() ?: return flowOf(null)
        return callbackFlow {
            val subscription = ref.addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.getLong("lastGlobalSync"))
                }
            }
            awaitClose { subscription.remove() }
        }
    }

    fun getDreamFlow(): Flow<Dream?> {
        val ref = getSettingsRef()?.parent?.document("user_dream") ?: return flowOf(null)
        return callbackFlow {
            val subscription = ref.addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject(Dream::class.java))
                } else {
                    trySend(null)
                }
            }
            awaitClose { subscription.remove() }
        }
    }

    suspend fun saveDream(dream: Dream) {
        getSettingsRef()?.parent?.document("user_dream")?.set(dream)?.await()
    }

    suspend fun deleteDream() {
        getSettingsRef()?.parent?.document("user_dream")?.delete()?.await()
    }

    suspend fun saveDisplayCurrency(code: Int) {
        getSettingsRef()?.set(mapOf("displayCurrency" to code), SetOptions.merge())?.await()
    }

    suspend fun getDisplayCurrency(): Int {
        return getSettingsRef()?.get()?.await()?.getLong("displayCurrency")?.toInt() ?: 980
    }

    fun getDisplayCurrencyFlow(): Flow<Int> {
        val ref = getSettingsRef() ?: return flowOf(980)
        return callbackFlow {
            val sub = ref.addSnapshotListener { s, _ ->
                trySend(s?.getLong("displayCurrency")?.toInt() ?: 980)
            }
            awaitClose { sub.remove() }
        }
    }

}