package com.denisshulika.fincentra.data.models.ui

import androidx.compose.ui.graphics.Color

data class EuropeanProvider(
    val id: String,
    val name: String,
    val providerCode: String,
    val countryName: String,
    val brandColor: Color
)

val EuropeanDemoBanks = listOf(
    EuropeanProvider(
        "revolut_eu",
        "Revolut",
        "fake_oauth_client_xf",
        "Global / EU",
        Color(0xFF000000)
    ),
    EuropeanProvider("db_de", "Deutsche Bank", "fake_client_xf", "Germany", Color(0xFF0033AA)),
    EuropeanProvider(
        "santander_es",
        "Santander",
        "fake_payment_status_oauth_client_xf",
        "Spain",
        Color(0xFFEC0000)
    ),
    EuropeanProvider(
        "bnp_fr",
        "BNP Paribas",
        "fake_interactive_decoupled_client_xf",
        "France",
        Color(0xFF00965E)
    ),
    EuropeanProvider(
        "bt_ro",
        "Banca Transilvania",
        "fake_interactive_client_xf",
        "Romania",
        Color(0xFFFBC02D)
    ),
    EuropeanProvider(
        "fake_biz",
        "Business Hub",
        "fake_delayed_oauth_client_xf",
        "EU Business",
        Color(0xFF455A64)
    )
)