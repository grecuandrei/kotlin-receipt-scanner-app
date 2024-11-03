package com.example.receiptscanner.second

data class ExpenseEntry(
    val expenseName: String,
    val amount: Double,
    val date: String, // For simplicity, this can be a string (format YYYY-MM-DD)
    val status: String,
    val category: String,
    val paymentMethod: String,
    val type: String
)

