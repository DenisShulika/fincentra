package com.denisshulika.fincentra.di

import com.denisshulika.fincentra.data.repository.FinanceRepository

object DependencyProvider {
    val repository = FinanceRepository()
}