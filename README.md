# LetMeCook 🍳

*Aplikasi resep yang tidak hanya memberimu ide, tapi juga teman ngobrol di dapur.*

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84)
![Language](https://img.shields.io/badge/Language-Java-DB4437)
![API](https://img.shields.io/badge/API-29%2B-blue)

LetMeCook adalah aplikasi resep makanan dan minuman yang dibuat untuk menemukan inspirasi memasak setiap hari. Jelajahi berbagai kategori, cari resep favoritmu, dan jika kamu bingung, tanyakan saja pada asisten AI kami yang siap membantumu (atau me-*roasting*-mu).

## ✨ Fitur-Fitur Keren

-   **🔍 Pencarian & Kategori**: Cari resep berdasarkan nama atau jelajahi kategori yang ada. Dari hidangan utama hingga pencuci mulut, semua ada di sini (tergantung API nya sih).
-   **🤖 Asisten AI "LetAICook"**: Bingung dengan langkah-langkah resep? Atau butuh ide lain? Tanyakan pada asisten AI kami yang didukung oleh **Google AI (Gemini)**. Dia ramah, tapi jangan coba-tanya di luar topik masak, ya!
-   **❤️ Resep Favorit**: Simpan resep yang kamu suka dengan satu ketukan. Semua resep favoritmu akan tersimpan secara lokal dan bisa diakses bahkan saat *offline*.
-   **📱 Tampilan Intuitif**: Antarmuka yang bersih dan mudah digunakan, dengan dukungan tema **Light & Dark Mode** agar nyaman di mata, kapan pun kamu memasak.

### 📸 Tampilan Aplikasi

**1. Tampilan Utama (Beranda & Pencarian)**

![Tampilan Utama](ss_home.png)

**2. Halaman Detail Resep**

![Detail Resep](ss_detail.png)

**3. Fitur Asisten AI "LetAICook"**

![Asisten AI](ss_ai.png)

## 🛠️ Implementasi Teknis

Aplikasi ini dibangun dengan fokus pada praktik Android modern dan komponen yang relevan.

-   **Arsitektur & Navigasi**:
    -   Menggunakan arsitektur **Single-Activity** dengan beberapa `Fragment` (`HomeFragment`, `FavoritesFragment`, `AiChatFragment`, `SettingsFragment`).
    -   Navigasi antar-`Fragment` dikelola sepenuhnya oleh **Navigation Component**.

-   **Pengambilan Data (Networking)**:
    -   Data resep diambil dari **TheMealDB API**.
    -   Koneksi jaringan ditangani oleh **Retrofit** dengan *converter* **Gson**, yang merupakan standar industri untuk komunikasi REST API di Android.
    -   Untuk memuat gambar dari URL, aplikasi ini mengandalkan *library* **Glide**.

-   **Penyimpanan Data Lokal**:
    -   **SQLite**: Resep favorit disimpan secara persisten menggunakan `SQLiteOpenHelper`, memungkinkan akses *offline*.
    -   **SharedPreferences**: Digunakan untuk menyimpan data ringan seperti preferensi tema (gelap/terang) dan riwayat obrolan dengan AI.

-   **Fitur Inovatif**:
    -   Integrasi dengan **Google AI (Gemini)** untuk fitur asisten masak. *Callback* dari API ditangani secara asinkron untuk menjaga performa UI.
    -   Tampilan respons AI menggunakan *library* **Markwon** untuk merender teks Markdown.

-   **UI & UX**:
    -   UI dibangun menggunakan komponen **Material 3**.
    -   Daftar data yang dinamis ditampilkan menggunakan `RecyclerView` untuk efisiensi memori.
    -   Animasi *loading* yang menarik menggunakan `Lottie`.

## 🚀 Cara Penggunaan

1.  **Clone Repositori**
    ```bash
    git clone https://github.com/restuahmadinata/letmecook.git
    ```
2.  **Tambahkan API Key**
    -   Buat berkas `local.properties` di direktori *root* proyek.
    -   Tambahkan *API key* Anda untuk Gemini AI dengan format berikut:
        ```properties
        GEMINI_API_KEY="YOUR_API_KEY_HERE"
        ```
    -   *Build* dan jalankan aplikasi.

## 📄 Lisensi

Lisensi apaan coeg? :"v