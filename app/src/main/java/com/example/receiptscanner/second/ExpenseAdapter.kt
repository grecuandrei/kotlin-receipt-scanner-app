package com.example.receiptscanner.second

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.receiptscanner.R

class ExpenseAdapter(
    private val groupedExpenses: List<SecondActivity.MonthExpenseGroup>,
    private val onExpenseClick: (ExpenseEntry) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val monthExpenseGroup = groupedExpenses[position]

        // Bind the month, expenses, and total amount for the month
        holder.bind(
            monthExpenseGroup.month,
            monthExpenseGroup.expenses,
            monthExpenseGroup.totalAmount // Pass the total amount
        )
    }

    override fun getItemCount(): Int {
        return groupedExpenses.size
    }

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val monthTextView: TextView = itemView.findViewById(R.id.monthTextView)
        private val monthTotalTextView: TextView = itemView.findViewById(R.id.monthTotalTextView) // Add this
        private val expensesContainer: ViewGroup = itemView.findViewById(R.id.expensesContainer)

        fun bind(month: String, expenses: List<ExpenseEntry>, totalAmount: Double) {
            // Set month text
            monthTextView.text = month

            // Set total amount for the month
            monthTotalTextView.text = "${String.format("%.2f", totalAmount)} Lei"

            // Clear the container for individual expenses before adding new ones
            expensesContainer.removeAllViews()

            // Add each expense row to the container
            for (expense in expenses) {
                val expenseView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.item_expense_row, expensesContainer, false)

                val expenseNameTextView = expenseView.findViewById<TextView>(R.id.expenseNameTextView)
                val amountTextView = expenseView.findViewById<TextView>(R.id.amountTextView)

                // Bind expense name and amount
                expenseNameTextView.text = expense.expenseName
                amountTextView.text = "${expense.amount} Lei"

                // Set click listener for each expense row
                expenseView.setOnClickListener {
                    onExpenseClick(expense) // Trigger the click listener
                }

                // Add the expense view to the container
                expensesContainer.addView(expenseView)
            }
        }
    }
}
