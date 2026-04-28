package com.denisshulika.fincentra.data.util

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
    "Small expenses are like small leaks in a ship – they can sink it. Watch your 'Others' category today!",
    "Your financial tree loves consistency. Add a manual transaction for any cash spent today.",
    "Today is a great day to review your 'Dream' progress. You are closer than you think!",
    "The best way to save money is not to spend it. Challenge: Zero spending on treats today!",
    "A budget tells your money where to go instead of wondering where it went. Check your limits.",
    "Invest in your future self. Your 'Safety Threshold' is your best friend.",
    "Financial peace isn't the acquisition of stuff. It's learning to live on less than you make.",
    "Your future self will thank you for the coffee you didn't buy today. Add it to your Dream!",
    "The goal is to be rich, not to look rich. Stay true to your financial plan.",
    "A blooming tree starts with a single drop of water. Every cent counts today."
)