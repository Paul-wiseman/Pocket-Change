# Currency Converter

## About

This Android application provides a simple and intuitive interface for converting between different currencies. Users can select their desired currencies, enter an amount to convert, and view the converted value along with any applicable commission fees. The app also allows users to manage their currency accounts, view exchange rates, and track their transaction history.

## Architecture

The app follows the **MVVM (Model-View-ViewModel)** architectural pattern, ensuring a clear separation of concerns and promoting testability. The key components are:

- **Model:** Represents the data and business logic of the app, including currency exchange rates, user accounts, and transaction history.
- **View:** Consists of the UI elements that display data and handle user interactions.
- **ViewModel:** Acts as an intermediary between the View and the Model, providing data to the View and handling user actions.

The app also utilizes **Clean Architecture** principles to further enhance modularity and maintainability.

## Technologies Used

- **Kotlin:** The primary programming language used for developing the app.
- **Dagger hilt:** For dependency Injection.
- **Coroutines:** For managing asynchronous operations and background tasks.
- **Flow:** For handling streams of data and reacting to changes.
- **Hilt:** For dependency injection, simplifying object creation and management.
- **Room:** For persisting data locally in a SQLite database.
- **Retrofit:** For making network requests to fetch currency exchange rates.
- **OkHttp:** For efficient HTTP communication.
- **Kotlinx Serialization:** For JSON parsing and serialization.
- **Material Design:** For creating a visually appealing and user-friendly interface.

## Features

- Real-time currency conversion
- Currency account management
- Exchange rate display
- Commission fee calculation
- Error handling


## Testing

The app includes comprehensive unit and integration tests to ensure code quality and functionality.

- **Unit Tests:** Test individual components in isolation using MockK and JUnit.
