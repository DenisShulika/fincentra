package com.denisshulika.fincentra.data.models.ui

import androidx.compose.ui.graphics.Color
import com.denisshulika.fincentra.R

data class EuropeanProvider(
    val id: String,
    val nameRes: Int,
    val providerCode: String,
    val countryNameRes: Int,
    val brandColor: Color
)

val EuropeanDemoBanks = listOf(
    EuropeanProvider(
        "revolut_eu",
        R.string.european_provider_revolut,
        "fake_oauth_client_xf",
        R.string.european_provider_revolut_country,
        Color(0xFF000000)
    ),
    EuropeanProvider(
        "db_de",
        R.string.european_provider_db,
        "fake_client_xf",
        R.string.european_provider_db_country,
        Color(0xFF0033AA)
    ),
    EuropeanProvider(
        "santander_es",
        R.string.european_provider_santander,
        "fake_payment_status_oauth_client_xf",
        R.string.european_provider_santander_country,
        Color(0xFFEC0000)
    ),
    EuropeanProvider(
        "bnp_fr",
        R.string.european_provider_bnp,
        "fake_interactive_decoupled_client_xf",
        R.string.european_provider_bnp_country,
        Color(0xFF00965E)
    ),
    EuropeanProvider(
        "bt_ro",
        R.string.european_provider_bt,
        "fake_interactive_client_xf",
        R.string.european_provider_bt_country,
        Color(0xFFFBC02D)
    ),
    EuropeanProvider(
        "fake_biz",
        R.string.european_provider_biz,
        "fake_delayed_oauth_client_xf",
        R.string.european_provider_biz_country,
        Color(0xFF455A64)
    )
)