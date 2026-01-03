package com.denisshulika.fincentra.data.models

import com.denisshulika.fincentra.R

data class BankProviderInfo(
    val id: String,
    val name: String,
    val logo: Int,
    val brandColor: androidx.compose.ui.graphics.Color
)

val SupportedBanks = listOf(
    BankProviderInfo(
        id = "Monobank",
        name = "Monobank",
        logo = R.drawable.monobank_logo ,
        brandColor = androidx.compose.ui.graphics.Color(0xFFE91E63)
    ),
)