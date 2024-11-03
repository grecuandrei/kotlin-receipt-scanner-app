package com.example.receiptscanner.first

data class QueryDatabaseRequest(
    val filter: Any? = null // Optional filter
)

data class QueryDatabaseResponse(
    val results: List<NotionPage>
)

data class NotionPage(
    val id: String,
    val properties: NotionProperties
)

data class NotionProperties(
    val Expense: PropertyValue,
    val Amount: PropertyValue,
    val Date: PropertyValue,
    val Status: PropertyValue,
    val Category: PropertyValue,
    val PaymentMethod: PropertyValue,
    val Type: PropertyValue
)

data class PageRequest(
    val parent: Parent,
    val properties: Map<String, PropertyValue>
)

data class Parent(
    val database_id: String
)

data class PropertyValue(
    val title: List<TextContent>? = null,
    val number: Double? = null,
    val date: DateProperty? = null,
    val select: SelectOption? = null,
    val rich_text: List<TextContent>? = null
)

data class TextContent(
    val text: Text
)

data class Text(
    val content: String
)

data class DateProperty(
    val start: String
)

data class SelectOption(
    val name: String
)
