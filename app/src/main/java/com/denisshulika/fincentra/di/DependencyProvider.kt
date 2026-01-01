package com.denisshulika.fincentra.di

import com.denisshulika.fincentra.data.network.common.BankProvider
import com.denisshulika.fincentra.data.network.monobank.MonobankApi
import com.denisshulika.fincentra.data.network.monobank.MonobankService
import com.denisshulika.fincentra.data.repository.FinanceRepository
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DependencyProvider {
    val repository = FinanceRepository()

    val monobankProvider: BankProvider = MonobankService()

    fun getInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.monobank.ua/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val monobankApi: MonobankApi = retrofit.create(MonobankApi::class.java)
}