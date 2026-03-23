package com.denisshulika.fincentra.di

import com.denisshulika.fincentra.data.network.common.BankProvider
import com.denisshulika.fincentra.data.network.monobank.MonobankApi
import com.denisshulika.fincentra.data.network.monobank.MonobankService
import com.denisshulika.fincentra.data.repository.AiRepository
import com.denisshulika.fincentra.data.repository.AuthRepository
import com.denisshulika.fincentra.data.repository.BudgetRepository
import com.denisshulika.fincentra.data.repository.FinanceRepository
import com.denisshulika.fincentra.data.repository.SettingsRepository
import com.denisshulika.fincentra.data.util.WidgetDataManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DependencyProvider {

    private lateinit var applicationContext: android.content.Context

    fun init(context: android.content.Context) {
        applicationContext = context.applicationContext
    }

    val financeRepository by lazy {
        FinanceRepository(
            db = getInstance(),
            auth = auth
        )
    }

    val budgetRepository by lazy {
        BudgetRepository(
            db = getInstance(),
            auth = auth
        )
    }

    val settingsRepository by lazy {
        SettingsRepository(
            db = getInstance(),
            auth = auth
        )
    }

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

    val aiRepository by lazy { AiRepository() }

    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val authRepository: AuthRepository by lazy { AuthRepository() }

    val widgetDataManager by lazy { WidgetDataManager(applicationContext) }

    fun getInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}