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
    const val SOURCE_CASH = "Готівка"
}

object BankProviders {
    const val MONOBANK = "Monobank"
    const val PRIVATBANK = "PrivatBank"
}

object BankAccountTypes {
    const val BLACK = "black"
    const val WHITE = "white"
    const val JAR = "jar"
    const val FOP = "fop"
}

object AuthConstants {
    const val WEB_CLIENT_ID = "531383896940-lk0qd97ohp6jaue4u9nunl8jpo6dg4th.apps.googleusercontent.com"
}