package com.denisshulika.fincentra.data.repository

import com.denisshulika.fincentra.data.models.domain.User
import com.denisshulika.fincentra.di.DependencyProvider
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.tasks.await

class AuthRepository {
    val auth = DependencyProvider.auth

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let {
            User(
                uid = it.uid,
                email = it.email ?: "",
                displayName = it.displayName ?: "",
                photoUrl = it.photoUrl?.toString() ?: ""
            )
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, name: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user

            val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                displayName = name
            }
            firebaseUser?.updateProfile(profileUpdates)?.await()

            val user = User(
                uid = firebaseUser?.uid ?: "",
                email = firebaseUser?.email ?: "",
                displayName = name
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user?.let { User(uid = it.uid, email = it.email ?: "") }
            if (user != null) Result.success(user) else Result.failure(Exception("USER_NOT_FOUND"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(credential: AuthCredential): Result<User> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user?.let {
                User(
                    uid = it.uid,
                    email = it.email ?: "",
                    displayName = it.displayName ?: "",
                    photoUrl = it.photoUrl?.toString() ?: ""
                )
            }
            if (user != null) Result.success(user) else Result.failure(Exception("GOOGLE_AUTH_FAILED"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSignInProvider(): String {
        return auth.currentUser?.providerData?.get(1)?.providerId ?: "password"
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            auth.currentUser?.updatePassword(newPassword)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUserAccount(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("USER_OFFLINE"))
        return try {
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}