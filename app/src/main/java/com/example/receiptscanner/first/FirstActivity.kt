package com.example.receiptscanner.first

import NotionApi
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.EditText
import android.widget.Toast
import android.Manifest
import android.app.Activity
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.receiptscanner.BuildConfig
import com.example.receiptscanner.R
import com.example.receiptscanner.databinding.ActivityFirstBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class FirstActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFirstBinding
    private var imageUri: Uri? = null
    private lateinit var captureImageLauncher: ActivityResultLauncher<Intent>
    private val databaseId = BuildConfig.NOTION_DATABASE_ID
    private val bearerToken = "Bearer ${BuildConfig.NOTION_BEARER_TOKEN}"
    private var selectedStatus: String? = null
    private var selectedCategory: String? = null
    private var selectedPaymentMethod: String? = null
    private var selectedType: String? = null

    // Retrofit initialization
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.notion.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // NotionApi
    val notionApi = retrofit.create(NotionApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the ActivityResultLauncher
        captureImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the image capture result here
                if (imageUri != null) {
                    processImageWithOCR(imageUri!!)
                }
            }
        }

        // Retrieve the selections passed from StartActivity
        selectedStatus = "Completed"
        selectedCategory = intent.getStringExtra("CATEGORY")
        selectedPaymentMethod = intent.getStringExtra("PAYMENT_METHOD")
        selectedType = intent.getStringExtra("TYPE")

        // Button to capture image
        binding.captureImageButton.setOnClickListener {
            checkCameraPermission()
        }

        // Button to register a new receipt from scratch
        binding.registerFromScratch.setOnClickListener {
            // Show dialog to confirm and edit the extracted details
            showDetailsDialog("", "", "", false)
        }
    }

    // Check for camera permission
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            captureImage()
        }
    }

    // Handle the permission result
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            captureImage()
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    // Capture image using the camera and save using MediaStore
    private fun captureImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            imageUri = createImageUri()
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            captureImageLauncher.launch(takePictureIntent)
        }
    }

    // Create a Uri for saving the image using MediaStore
    private fun createImageUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "receipt_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    // OCR Processing
    private fun processImageWithOCR(imageUri: Uri) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromFilePath(this, imageUri)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val ocrText = visionText.text
                Log.d("OCRResult", "Extracted OCR Text: $ocrText") // Log the full OCR text for debugging
                extractDetailsFromOCR(ocrText)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            }
    }

    // Extract relevant data (merchant, date, amount) from the OCR text
    private fun extractDetailsFromOCR(ocrText: String) {
        // Extract merchant, date, and amount using regex
        val merchant = extractMerchant(ocrText)
        val date = extractDate(ocrText)
        val amount = extractAmount(ocrText)

        // Show dialog to confirm and edit the extracted details
        showDetailsDialog(merchant, date, amount, true)
    }

    // Helper function to extract merchant (first line of receipt)
    private fun extractMerchant(text: String): String {
        // Assuming merchant name is at the top of the receipt (first non-empty line)
        return text.lines().firstOrNull { it.isNotBlank() } ?: "Unknown Merchant"
    }

    // Helper function to extract date (searching for "DATA: dd/MM/yyyy" or "DATE: dd/MM/yyyy")
    private fun extractDate(text: String): String {
        // Pattern to match "DATA: dd/MM/yyyy" or "DATE: dd/MM/yyyy"
        val dateRegex = Regex("(DATA|DATE):\\s*(\\d{2}/\\d{2}/\\d{4})")
        val match = dateRegex.find(text)
        return match?.groupValues?.get(2) ?: SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())
    }

    // Helper function to extract amount from OCR text
    private fun extractAmount(text: String): String {
        // Pattern to match consecutive occurrences of the same amount (e.g., "11.94" appears twice)
        val repeatedAmountRegex = Regex("([0-9]+[.,][0-9]{2})\\s*\\1")
        val repeatedMatch = repeatedAmountRegex.find(text)

        // If we find two consecutive occurrences of the same amount, return it
        if (repeatedMatch != null) {
            return repeatedMatch.groupValues[1].replace(",", ".")
        }

        // Otherwise, fall back to searching for "TOTAL" followed by the amount
        val totalRegex = Regex("TOTAL[:\\s]*([0-9]+[.,][0-9]{2})")
        val totalMatch = totalRegex.find(text)

        // If "TOTAL" is found, return the amount after it
        if (totalMatch != null) {
            return totalMatch.groupValues[1].replace(",", ".")
        }

        // Fallback: Extract the largest number if no "TOTAL" is found or no consecutive amounts found
        val allAmountsRegex = Regex("([0-9]+[.,][0-9]{2})")
        val allMatches = allAmountsRegex.findAll(text)

        // Convert all matches to a list of numbers
        val amounts = allMatches.map { it.groupValues[1].replace(",", ".").toDouble() }.toList()

        // Return the largest amount found, or "0.00" if none found
        return if (amounts.isNotEmpty()) {
            String.format("%.2f", amounts.maxOrNull() ?: 0.00)
        } else {
            "0.00"
        }
    }

    // Show dialog with pre-filled fields
    private fun showDetailsDialog(merchant: String, date: String, amount: String, fromOCR: Boolean) {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_receipt_details, null)

        // Auto-filled fields from OCR
        val merchantInput = dialogLayout.findViewById<EditText>(R.id.inputMerchant)
        val dateInput = dialogLayout.findViewById<EditText>(R.id.inputDate)
        val amountInput = dialogLayout.findViewById<EditText>(R.id.inputAmount)

        // Fields for manual selection
        val statusSpinner = dialogLayout.findViewById<Spinner>(R.id.inputStatus)
        val categorySpinner = dialogLayout.findViewById<Spinner>(R.id.inputCategory)
        val paymentMethodSpinner = dialogLayout.findViewById<Spinner>(R.id.inputPaymentMethod)
        val typeSpinner = dialogLayout.findViewById<Spinner>(R.id.inputType)

        // Set extracted data from OCR
        if (fromOCR) {
            merchantInput.setText(merchant)
            dateInput.setText(date)
            amountInput.setText(amount)
        }

        // Pre-fill the spinners with the choices passed from StartActivity
        val statusAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Completed", "Estimated", "Upcoming")
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusSpinner.adapter = statusAdapter
        statusSpinner.setSelection(statusAdapter.getPosition(selectedStatus)) // Set pre-filled category

        val categoryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Fuel", "Personal", "Clothes", "Home", "Food", "Entertainment", "SRL")
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter
        categorySpinner.setSelection(categoryAdapter.getPosition(selectedCategory)) // Set pre-filled category

        val paymentMethodAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Revolut", "Cash", "BT", "Coupon Card", "CEC")
        )
        paymentMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paymentMethodSpinner.adapter = paymentMethodAdapter
        paymentMethodSpinner.setSelection(paymentMethodAdapter.getPosition(selectedPaymentMethod)) // Set pre-filled payment method

        val typeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Non-essential", "Essential")
        )
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = typeAdapter
        typeSpinner.setSelection(typeAdapter.getPosition(selectedType)) // Set pre-filled type

        dialogBuilder.setView(dialogLayout)
        dialogBuilder.setPositiveButton("Save") { _, _ ->
            // Collect user input
            val enteredMerchant = merchantInput.text.toString()
            val enteredDate = dateInput.text.toString()
            val enteredAmount = amountInput.text.toString()
            val selectedStatus = categorySpinner.selectedItem.toString()
            val selectedCategory = categorySpinner.selectedItem.toString()
            val selectedPaymentMethod = paymentMethodSpinner.selectedItem.toString()
            val selectedType = typeSpinner.selectedItem.toString()

            // Send the data to Notion
            sendDataToNotion(enteredMerchant, enteredDate, enteredAmount, selectedStatus, selectedCategory, selectedPaymentMethod, selectedType)
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        dialogBuilder.show()
    }

    private fun formatDate(ocrDate: String): String {
        // Define the input format based on your OCR date format (dd/MM/yyyy)
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        // Define the output format as ISO 8601 (yyyy-MM-dd)
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        return try {
            val date = inputFormat.parse(ocrDate) // Parse the date from OCR
            outputFormat.format(date!!) // Return the formatted date
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to current date in ISO format if parsing fails
            outputFormat.format(Date())
        }
    }

    // Send the data to Notion (simple example, you will need your Notion API setup here)
    private fun sendDataToNotion(
        merchant: String,
        date: String,
        amount: String,
        status: String,
        category: String,
        paymentMethod: String,
        type: String
    ) {
        // Format the date properly
        val formattedDate = formatDate(date)

        CoroutineScope(Dispatchers.IO).launch {
            val pageRequest = PageRequest(
                parent = Parent(databaseId),
                properties = mapOf(
                    "Expense" to PropertyValue(title = listOf(TextContent(Text(merchant)))),
                    "Amount" to PropertyValue(number = amount.toDoubleOrNull() ?: 0.0),

                    // Use formatted date in the correct ISO 8601 format
                    "Date" to PropertyValue(date = DateProperty(start = formattedDate)),

                    // Status, Category, Payment Method, and Type should be formatted as "select"
                    "Status" to PropertyValue(select = SelectOption(name = status)),
                    "Category" to PropertyValue(select = SelectOption(name = category)),
                    "Payment Method" to PropertyValue(select = SelectOption(name = paymentMethod)),
                    "Type" to PropertyValue(select = SelectOption(name = type))
                )
            )

            try {
                val response = notionApi.createPage(bearerToken, pageRequest)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@FirstActivity, "Data sent to Notion", Toast.LENGTH_SHORT).show()

                        finish()
                    } else {
                        val responseBody = response.errorBody()?.string() ?: "No error body"
                        val responseCode = response.code()
                        Toast.makeText(this@FirstActivity, "Failed to send data: $responseCode", Toast.LENGTH_SHORT).show()
                        Log.e("NotionAPI", "Response Code: $responseCode, Response Body: $responseBody")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FirstActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("NotionAPI", "Exception: ${e.message}")
                }
            }
        }
    }
}
