package com.denisshulika.fincentra.di

import com.denisshulika.fincentra.data.network.MonobankApi
import com.denisshulika.fincentra.data.repository.FinanceRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.firebase.firestore.FirebaseFirestore

object DependencyProvider {
    val repository = FinanceRepository()

    fun getInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.monobank.ua/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val monobankApi: MonobankApi = retrofit.create(MonobankApi::class.java)
}