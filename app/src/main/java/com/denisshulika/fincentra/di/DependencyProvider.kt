package com.denisshulika.fincentra.di

import com.denisshulika.fincentra.data.network.common.BankProvider
import com.denisshulika.fincentra.data.network.monobank.MonobankApi
import com.denisshulika.fincentra.data.network.monobank.MonobankService
import com.denisshulika.fincentra.data.repository.AuthRepository
import com.denisshulika.fincentra.data.repository.FinanceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DependencyProvider {
    val repository by lazy { FinanceRepository() }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.monobank.ua/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val monobankApi: MonobankApi by lazy {
        retrofit.create(MonobankApi::class.java)
    }

    val monobankProvider: BankProvider by lazy {
        MonobankService()
    }

    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val authRepository: AuthRepository by lazy { AuthRepository() }

    fun getInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}