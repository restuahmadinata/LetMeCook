# LetMeCook

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84)
![Language](https://img.shields.io/badge/Language-Java-DB4437)
![API](https://img.shields.io/badge/API-29%2B-blue)

LetMeCook is a food and beverage recipe application created to find daily cooking inspiration. Explore various categories, search for your favorite recipes, and if you're confused, just ask our AI assistant who is ready to help you (or roast you).

## Cool Features

-   ** Search & Categories**: Search for recipes by name or explore existing categories. From main dishes to desserts, everything is here (depends on the API though).
-   ** AI Assistant "LetAICook"**: Confused with recipe steps? Or need other ideas? Ask our AI assistant powered by **Google AI (Gemini)**. It's friendly, but don't try asking off-topic cooking questions!
-   ** Favorite Recipes**: Save recipes you like with one tap. All your favorite recipes will be saved locally and can be accessed even when offline.
-   ** Intuitive Display**: Clean and easy-to-use interface, with **Light & Dark Mode** theme support to be easy on the eyes, whenever you cook.

### App Screenshots

| Feature | Screenshot |
|---------|-----------|
| Main View (Home & Search) | ![Main View](ss_home.png) |
| Recipe Detail Page | ![Recipe Detail](ss_detail.png) |
| AI Assistant "LetAICook" Feature | ![AI Assistant](ss_ai.png) |

## Technical Implementation

This application is built with a focus on modern Android practices and relevant components.

-   **Architecture & Navigation**:
    -   Uses **Single-Activity** architecture with multiple `Fragment`s (`HomeFragment`, `FavoritesFragment`, `AiChatFragment`, `SettingsFragment`).
    -   Navigation between `Fragment`s is fully managed by **Navigation Component**.

-   **Data Fetching (Networking)**:
    -   Recipe data is fetched from **TheMealDB API**.
    -   Network connections are handled by **Retrofit** with **Gson** converter, which is an industry standard for REST API communication on Android.
    -   To load images from URLs, this application relies on the **Glide** library.

-   **Local Data Storage**:
    -   **SQLite**: Favorite recipes are stored persistently using `SQLiteOpenHelper`, allowing offline access.
    -   **SharedPreferences**: Used to store lightweight data such as theme preferences (dark/light) and chat history with AI.

-   **Innovative Features**:
    -   Integration with **Google AI (Gemini)** for cooking assistant feature. Callbacks from the API are handled asynchronously to maintain UI performance.
    -   AI response display uses the **Markwon** library to render Markdown text.

-   **UI & UX**:
    -   UI is built using **Material 3** components.
    -   Dynamic data lists are displayed using `RecyclerView` for memory efficiency.
    -   Attractive loading animations using `Lottie`.

## How to Use

1.  **Clone Repository**
    ```bash
    git clone https://github.com/restuahmadinata/letmecook.git
    ```
2.  **Add API Key**
    -   Create a `local.properties` file in the project root directory.
    -   Add your API key for Gemini AI with the following format:
        ```properties
        GEMINI_API_KEY="YOUR_API_KEY_HERE"
        ```
    -   Build and run the application.

## 📄 License

What license, dude? :"v
