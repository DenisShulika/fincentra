package com.denisshulika.fincentra.data.models.ui

import androidx.compose.ui.graphics.Color
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.util.BankProviders

data class BankProviderInfo(
    val id: String,
    val name: String,
    val logo: Int,
    val brandColor: Color
)

val SupportedBanks = listOf(
    BankProviderInfo(
        id = BankProviders.MONOBANK,
        name = BankProviders.MONOBANK,
        logo = R.drawable.monobank_logo ,
        brandColor = Color(0xFFE91E63)
    ),
)