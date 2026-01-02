package com.denisshulika.fincentra.data.network.monobank.models

data class MonoTransactionResponse(
    val id: String,
    val time: Long,
    val description: String,
    val mcc: Int,
    val originalMcc: Int,
    val hold: Boolean,
    val amount: Long,
    val operationAmount: Long,
    val currencyCode: Int,
    val commissionRate: Long,
    val cashbackAmount: Long,
    val balance: Long,
    
    val comment: String? = null,
    val receiptId: String? = null,
    val invoiceId: String? = null,
    val counterEdrpou: String? = null,
    val counterIban: String? = null,
    val counterName: String? = null
)