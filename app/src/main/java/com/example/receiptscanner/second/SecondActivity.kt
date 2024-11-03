package com.example.receiptscanner.second

import NotionApi
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.receiptscanner.BuildConfig
import com.example.receiptscanner.R
import java.text.SimpleDateFormat
import java.util.Locale

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SecondActivity : AppCompatActivity() {

    private lateinit var expenseRecyclerView: RecyclerView
    private val databaseId = BuildConfig.NOTION_DATABASE_ID
    private val bearerToken = "Bearer ${BuildConfig.NOTION_BEARER_TOKEN}"

    // Retrofit initialization
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.notion.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // NotionApi
    val notionApi = retrofit.create(NotionApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        expenseRecyclerView = findViewById(R.id.expenseRecyclerView)

        // Fetch and display data
        CoroutineScope(Dispatchers.IO).launch {
            val expenses = fetchExpensesFromNotion()

            // Group expenses by month and sort by date within each month
            val groupedExpenses = groupByMonth(expenses)

            withContext(Dispatchers.Main) {
                // Set adapter with the grouped (and sorted) expenses
                val adapter = ExpenseAdapter(groupedExpenses) { selectedExpense ->
                    showExpenseDetailsDialog(selectedExpense)
                }
                expenseRecyclerView.adapter = adapter
                expenseRecyclerView.layoutManager = LinearLayoutManager(this@SecondActivity)
            }
        }
    }

    // Fetch expenses from Notion (as defined earlier)
    private suspend fun fetchExpensesFromNotion(): List<ExpenseEntry> {
        // Assuming NOTION_DATABASE_ID is the correct ID for your database
        val response = notionApi.queryDatabase(bearerToken, databaseId)

        if (response.isSuccessful && response.body() != null) {
            val notionPages = response.body()!!.results

            // Log the entire response for debugging
            Log.d("NotionAPI", "Response: ${response.body()}")

            // Convert Notion pages to ExpenseEntry list with null checks
            return notionPages.map { page ->
                ExpenseEntry(
                    expenseName = page.properties.Expense.title?.firstOrNull()?.text?.content ?: "Unknown",
                    amount = page.properties.Amount.number ?: 0.0,
                    date = page.properties.Date.date?.start ?: "Unknown",

                    // Safely access the select property, provide a default if null
                    status = page.properties.Status?.select?.name ?: "Unknown",
                    category = page.properties.Category?.select?.name ?: "Unknown",
                    paymentMethod = page.properties.PaymentMethod?.select?.name ?: "Unknown",
                    type = page.properties.Type?.select?.name ?: "Unknown"
                )
            }
        } else {
            Log.e("NotionAPI", "Failed to fetch data: ${response.errorBody()?.string()}")
            return emptyList() // Return an empty list in case of failure
        }
    }

    data class MonthExpenseGroup(
        val month: String,
        val expenses: List<ExpenseEntry>,
        val totalAmount: Double // Add the total amount for the month
    )

    private fun groupByMonth(expenses: List<ExpenseEntry>): List<MonthExpenseGroup> {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

        // Sort expenses by date in descending order
        val sortedExpenses = expenses.sortedByDescending {
            inputFormat.parse(it.date)
        }

        // Group by month and calculate the total amount for each month
        return sortedExpenses.groupBy { expense ->
            val date = inputFormat.parse(expense.date)
            outputFormat.format(date!!)
        }.map { (month, expensesInMonth) ->
            // Calculate the total amount for the month
            val totalAmount = expensesInMonth.sumOf { it.amount }
            MonthExpenseGroup(month, expensesInMonth, totalAmount)
        }
    }

    // Show expense details dialog (as defined earlier)
    private fun showExpenseDetailsDialog(expense: ExpenseEntry) {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_expense_details, null)

        // Bind data to dialog views
        val expenseNameTextView = dialogLayout.findViewById<TextView>(R.id.dialogExpenseName)
        val amountTextView = dialogLayout.findViewById<TextView>(R.id.dialogAmount)
        val dateTextView = dialogLayout.findViewById<TextView>(R.id.dialogDate)
        val statusTextView = dialogLayout.findViewById<TextView>(R.id.dialogStatus)
        val categoryTextView = dialogLayout.findViewById<TextView>(R.id.dialogCategory)
        val paymentMethodTextView = dialogLayout.findViewById<TextView>(R.id.dialogPaymentMethod)
        val typeTextView = dialogLayout.findViewById<TextView>(R.id.dialogType)

        // Set data
        expenseNameTextView.text = expense.expenseName
        amountTextView.text = "${expense.amount} Lei"

        // Format date from yyyy-MM-dd to dd/MM/yyyy
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        val formattedDate = try {
            val date = inputFormat.parse(expense.date)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            expense.date // If parsing fails, fall back to the original date format
        }

        dateTextView.text = formattedDate
        statusTextView.text = expense.status
        categoryTextView.text = expense.category
        paymentMethodTextView.text = expense.paymentMethod
        typeTextView.text = expense.type

        dialogBuilder.setView(dialogLayout)
        dialogBuilder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
        dialogBuilder.show()
    }
}

