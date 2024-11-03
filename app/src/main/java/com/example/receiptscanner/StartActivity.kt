package com.example.receiptscanner

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.receiptscanner.databinding.ActivityStartBinding
import com.example.receiptscanner.first.FirstActivity
import com.example.receiptscanner.second.SecondActivity

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    // Variables to store the selected values
    private var selectedCategory: String? = null
    private var selectedPaymentMethod: String? = null
    private var selectedType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigate to FirstActivity for the scanner
        binding.buttonRegisterReceipt.setOnClickListener {
            // Start the flow of category, payment method, and type selection
            showCategoryChooserDialog()
        }

        // Navigate to SecondActivity for viewing receipts
        binding.buttonSeeReceiptList.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
    }

    // 1. Show Category Chooser Dialog
    private fun showCategoryChooserDialog() {
        val categories = arrayOf("Fuel", "Personal", "Clothes", "Home", "Food", "Entertainment", "SRL")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Category chooser:")
        builder.setItems(categories) { _, which ->
            selectedCategory = categories[which]
            showPaymentMethodChooserDialog() // Proceed to next dialog
        }
        builder.setCancelable(true)
        builder.show()
    }

    // 2. Show Payment Method Chooser Dialog
    private fun showPaymentMethodChooserDialog() {
        val paymentMethods = arrayOf("Revolut", "Cash", "BT", "Coupon Card", "CEC")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Payment Method chooser:")
        builder.setItems(paymentMethods) { _, which ->
            selectedPaymentMethod = paymentMethods[which]
            showTypeChooserDialog() // Proceed to next dialog
        }
        builder.setCancelable(false)
        builder.show()
    }

    // 3. Show Type Chooser Dialog
    private fun showTypeChooserDialog() {
        val types = arrayOf("Non-essential", "Essential")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Type chooser:")
        builder.setItems(types) { _, which ->
            selectedType = types[which]
            proceedToFirstActivity() // After all selections, go to FirstActivity
        }
        builder.setCancelable(false)
        builder.show()
    }

    // After all selections are made, proceed to FirstActivity
    private fun proceedToFirstActivity() {
        val intent = Intent(this, FirstActivity::class.java)
        // Pass selected values to FirstActivity
        intent.putExtra("CATEGORY", selectedCategory)
        intent.putExtra("PAYMENT_METHOD", selectedPaymentMethod)
        intent.putExtra("TYPE", selectedType)
        startActivity(intent)
    }
}
