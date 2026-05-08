package com.denisshulika.fincentra.data.models.ui

import androidx.compose.ui.graphics.Color
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.util.BankProviders

data class BankProviderInfo(
    val id: String,
    val nameRes: Int,
    val logo: Int,
    val brandColor: Color,
    val subtitleRes: Int = 0
)

val SupportedBanks = listOf(
    BankProviderInfo(
        id = BankProviders.MONOBANK,
        nameRes = R.string.bank_provider_info_mono_name,
        logo = R.drawable.monobank_logo,
        brandColor = Color(0xFFE91E63)
    ),
    BankProviderInfo(
        id = BankProviders.WISE,
        nameRes = R.string.bank_provider_info_wise_name,
        logo = R.drawable.wise_logo,
        brandColor = Color(0xFF00B67A),
        subtitleRes = R.string.bank_provider_info_wise_subtitle
    ),
    BankProviderInfo(
        id = BankProviders.GOOGLE_WALLET,
        nameRes = R.string.bank_provider_info_wallet_name,
        logo = R.drawable.google_icon,
        brandColor = Color(0xFF4285F4),
        subtitleRes = R.string.bank_provider_info_wallet_subtitle
    )
)