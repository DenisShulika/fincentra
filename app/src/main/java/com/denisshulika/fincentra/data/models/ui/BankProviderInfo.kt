package com.denisshulika.fincentra.data.models.ui

import androidx.compose.ui.graphics.Color
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.util.BankProviders

data class BankProviderInfo(
    val id: String,
    val name: String,
    val logo: Int,
    val brandColor: Color,
    val subtitle: String = ""
)

val SupportedBanks = listOf(
    BankProviderInfo(
        id = BankProviders.MONOBANK,
        name = "Monobank",
        logo = R.drawable.monobank_logo,
        brandColor = Color(0xFFE91E63)
    ),
    BankProviderInfo(
        id = BankProviders.GOOGLE_WALLET,
        name = "Google Wallet",
        logo = R.drawable.google_icon,
        brandColor = Color(0xFF4285F4),
        subtitle = "NFC Notifications"
    )
)