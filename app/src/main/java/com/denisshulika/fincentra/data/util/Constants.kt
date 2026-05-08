package com.denisshulika.fincentra.data.util

import com.denisshulika.fincentra.R

object FirestoreCollections {
    const val USERS = "users"
    const val TRANSACTIONS = "transactions"
    const val ACCOUNTS = "accounts"
    const val SETTINGS = "settings"
}

object FirestoreDocuments {
    const val USER_SETTINGS = "user_settings"
    const val SYNC_METADATA = "sync_metadata"
}

object TransactionConstants {
    const val ACCOUNT_ID_MANUAL = "manual"
    const val SOURCE_CASH = "Cash"
}

object FilterConstants {
    const val ALL = "ALL"
    const val EXPENSES = "EXPENSES"
    const val INCOME = "INCOME"
}

object BankProviders {
    const val MONOBANK = "Monobank"
    const val WISE = "Wise"
    const val GOOGLE_WALLET = "GoogleWallet"
}

object BankAccountTypes {
    const val BLACK = "black"
    const val WHITE = "white"
    const val JAR = "jar"
    const val FOP = "fop"
}

object AuthConstants {
    const val WEB_CLIENT_ID =
        "531383896940-lk0qd97ohp6jaue4u9nunl8jpo6dg4th.apps.googleusercontent.com"
}

object PrefConstants {
    const val PREFS_NAME = "fincentra_prefs"
    const val KEY_IS_ONBOARDING_COMPLETED = "is_onboarding_completed"
}

object CurrencyConstants {
    const val EXCHANGE_RATE_API_URL = "https://open.er-api.com/v6/latest/"
    const val DEFAULT_DISPLAY_CURRENCY = 980
}

val FinancialPredictions = listOf(
    R.string.constants_prediction_1,
    R.string.constants_prediction_2,
    R.string.constants_prediction_3,
    R.string.constants_prediction_4,
    R.string.constants_prediction_5,
    R.string.constants_prediction_6,
    R.string.constants_prediction_7,
    R.string.constants_prediction_8,
    R.string.constants_prediction_9,
    R.string.constants_prediction_10
)