# Receipt Scanner

An Android app for scanning receipts and organizing data. This app utilizes ML Kit for OCR (Optical Character Recognition) to extract text from receipts and then saves the parsed information to a Notion database using the Notion API.

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Setup](#setup)
- [Configuration](#configuration)
- [Usage](#usage)
- [Dependencies](#dependencies)
- [License](#license)

## Features

- **Receipt Scanning**: Uses Google ML Kit's OCR feature to scan and extract text from receipts.
- **Save Data to Notion**: Sends extracted data to a Notion database via Notion API integration.
- **Token Storage**: Uses a configuration file to store API keys and tokens, which are not included in version control.
- **Data Visualization** (Soon) : Displays pie charts of monthly expenditures using a charting library. For now only as a list.

## Requirements

- Android Studio Flamingo or newer
- Minimum SDK: 26
- Target SDK: 34
- Notion API integration with a valid API token and a database set up in Notion

## Setup

1. **Clone the Repository**:
   ```
   git clone https://github.com/yourusername/receipt-scanner.git
   cd receipt-scanner
   ```

2. **Configuration**:
   - Create a `config.properties` file in the `app` directory (not tracked by Git).
   - Add your Notion API token and database ID in `config.properties`:
     ```
     NOTION_BEARER_TOKEN=your_bearer_token_here
     NOTION_DATABASE_ID=your_database_id_here
     ```

3. **Gradle Sync**:
   - Open the project in Android Studio and sync Gradle to download all dependencies.
4. **Grant Permissions**:
   - The app will prompt for permissions to access the camera.

## Configuration
The app uses the BuildConfig class to access secure properties. Ensure that `config.properties` contains:
```
NOTION_BEARER_TOKEN=your_bearer_token_here
NOTION_DATABASE_ID=your_database_id_here
```
These values will be included in the `BuildConfig` automatically through Gradle.

### Accessing Configuration Values
In your code, access these values via:
```
private val bearerToken = BuildConfig.NOTION_BEARER_TOKEN
private val databaseId = BuildConfig.NOTION_DATABASE_ID
```

### Hiding Configuration Values
The `config.properties` file should not be committed to version control. Ensure it’s added to `.gitignore`:
```
# Ignore configuration files with sensitive information
config.properties
```

## Usage

1. **Scan a Receipt**:
   - Open the app and use the camera feature to capture an image of a receipt.
   - The app will process the image, extract text, and parse relevant information.

2. **Save to Notion**:
   - The parsed data is sent to the specified Notion database.

3. **View Data**:
   - View monthly expenditures or other analytics as list (soon with charts) within the app.

## Dependencies

- **ML Kit**: Text recognition for OCR.
- **Notion API Integration**: For sending parsed data to Notion.
- **Retrofit**: For API communication.
- **Kotlin Coroutines**: For asynchronous operations.

## License
This project is open-source and available under the [MIT License](https://github.com/grecuandrei/kotlin-receipt-scanner-app/blob/master/LICENSE).
