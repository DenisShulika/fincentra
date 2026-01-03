package com.denisshulika.fincentra.data.repository

import com.denisshulika.fincentra.data.models.User
import com.denisshulika.fincentra.di.DependencyProvider
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = DependencyProvider.auth

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

    suspend fun signUpWithEmail(email: String, pass: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user?.let { User(uid = it.uid, email = it.email ?: "") }
            if (user != null) Result.success(user) else Result.failure(Exception("Помилка реєстрації"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user?.let { User(uid = it.uid, email = it.email ?: "") }
            if (user != null) Result.success(user) else Result.failure(Exception("Користувача не знайдено"))
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
            if (user != null) Result.success(user) else Result.failure(Exception("Google Auth Failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}