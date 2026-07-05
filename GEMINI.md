# Gemini & Agentic Workflow: Cassy Kasir

Dokumen ini mendefinisikan standar teknis, arsitektur, dan protokol interaksi untuk pengembangan proyek Cassy Kasir. Setiap kontribusi kode wajib mematuhi panduan ini untuk menjaga kualitas, konsistensi, dan skalabilitas.

---

## 1. Arsitektur Proyek (MVI-Lite)

Proyek ini menggunakan pola arsitektur **MVI-Lite** (Model-View-Intent) yang disederhanakan dengan struktur folder sebagai berikut:

- **UI Layer (`antarmuka`)**: Komposabel stateless dan UI logic.
- **Logic Layer (`ViewModel`)**: Pengelola status layar dan pemicu aksi.
- **Domain Layer (`ranah`)**: Logika bisnis murni, Use Cases, dan Model Data.
- **Data Layer (`data`)**: (Akan datang) Repositori dan persistensi (Room).

**Aturan Emas**:
- Aliran data searah (UDF): View -> Intent (Aksi) -> ViewModel -> State -> View.
- Pemisahan tegas: UI tidak boleh menyentuh database atau logika bisnis berat secara langsung.

---

## 2. Naming & Bahasa (Technical Indonesian)

Gunakan Bahasa Indonesia teknis yang baku untuk seluruh simbol kode (kecuali istilah teknis yang tidak ada padanannya).

### Konvensi Nama:
- **Kelas/Objek**: Kata Benda (misal: `PengelolaKeranjang`, `RepositoriProduk`).
- **Fungsi**: Kata Kerja + Objek (misal: `hitungTotal()`, `ambilDaftarProduk()`).
- **Variabel**: Deskriptif (misal: `hargaSatuan`, `apakahMemuat`).
- **Boolean**: Awalan `apakah` atau `sudah` (misal: `apakahAktif`, `sudahBayar`).

#### Tabel Referensi Naming:

| Bahasa Inggris      | Bahasa Indonesia (Target) | Konteks           |
|:--------------------|:--------------------------|:------------------|
| `get` / `fetch`     | `ambil` / `muat`          | Fungsi Data       |
| `is` / `has`        | `apakah` / `punya`        | Boolean           |
| `loading`           | `sedangMemuat`            | Status/State      |
| `success`           | `berhasil`                | Status/State      |
| `error` / `failure` | `gagal` / `salah`         | Status/State      |
| `update` / `edit`   | `ubah` / `perbarui`       | Fungsi/Aksi       |
| `send` / `emit`     | `kirim`                   | Fungsi Efek/Event |
| `save` / `delete`   | `simpan` / `hapus`        | Fungsi Data       |

---

## 3. KDocs & Dokumentasi

Setiap anggota publik (kelas, fungsi, properti) **wajib** memiliki dokumentasi KDoc dalam Bahasa Indonesia.

- **Fungsi**: Jelaskan tujuan, parameter (@param), dan nilai balik (@return).
- **Kelas**: Jelaskan peran utama dalam sistem.
- **Komentar Inline**: Gunakan hanya untuk menjelaskan "mengapa" (bukan "apa") pada logika yang kompleks.

---

## 4. State Management & UDF

- **Immutable State**: Gunakan `data class` dengan properti `val`. Jangan pernah gunakan variabel yang dapat diubah (`var`) di dalam status.
- **StateFlow**: Gunakan `MutableStateFlow` (private) dan `StateFlow` (public) di ViewModel.
- **Update Atomik**: Perbarui status hanya melalui fungsi `.update { ... }` untuk menjamin keamanan konkurensi.
- **Aksi Tersegel**: Definisikan semua interaksi pengguna menggunakan `sealed interface` atau `sealed class` (misal: `AksiLayarUtama`).

---

## 5. Coroutine & Asynchronous

- **Scope**: Gunakan `viewModelScope` di ViewModel dan `LaunchedEffect` di UI.
- **Dispatchers**: Jangan *hardcode* Dispatchers. Suntikkan (inject) Dispatchers jika memungkinkan untuk mempermudah pengujian.
- **Safety**: Pastikan fungsi *suspend* aman dijalankan dari Main thread (main-safe).
- **Error Handling**: Gunakan `try-catch` di dalam coroutine untuk menangani kegagalan asinkron tanpa menghentikan aplikasi.

---

## 6. Commit Message

Gunakan format pesan commit yang deskriptif dalam Bahasa Indonesia:

**Format**: `Scope [Label]: [Deskripsi singkat]`

**Contoh**:
- `Scope E: implementasi status loading dan gagal pada layar detail`
- `Scope F: integrasi aksi tambah ke keranjang dari layar detail`
- `Scope G: perbaikan bug navigasi pada tablet`

---

## 7. Navigation & Routing

- **Centralized**: Simpan rute dan argumen di satu tempat (misal: `TujuanNavigasiKasir`).
- **Type-Safe**: Gunakan argumen navigasi yang didefinisikan secara tegas.
- **Result Passing**: Gunakan `SavedStateHandle` untuk mengirim hasil balik antar layar (misal: pesan sukses setelah kembali).
- **Backstack**: Kelola *backstack* dengan bijak untuk menghindari penumpukan layar yang sama.

---

## 8. UI/UX & Ergonomics (Phone & Tablet)

- **Adaptive Layout**: Gunakan `BoxWithConstraints` atau `WindowSizeClass` untuk membedakan tata letak ponsel (kolom tunggal) dan tablet (multi-panel).
- **Ergonomi**:
    - Letakkan aksi utama di area yang mudah dijangkau jempol (bawah/tengah).
    - Gunakan *spacing* (padding/margin) yang konsisten (kelipatan 4dp atau 8dp).
- **Stateless UI**: Komposabel murni harus menerima data melalui parameter dan mengirim aksi melalui callback.

---

## 9. Android API & Context

- **Modern API**: Gunakan API terbaru yang stabil (saat ini menargetkan Android 15/API 35 via AGP 9.x).
- **Context Safety**: Hindari kebocoran memori dengan tidak menyimpan `Context` di ViewModel atau kelas berumur panjang.
- **Resources**: Ambil string, warna, dan dimensi dari XML resources melalui `stringResource`, dsb.

---

## 10. Copywriting Constraints (UX Writing)

Gunakan gaya bahasa manusiawi, ringkas, dan langsung:
- **Aktif**: "Hapus produk" (bukan "Produk dihapus").
- **Solutif**: "Koneksi terputus. Coba lagi?" (bukan "Error 404").
- **Memotivasi**: "Keranjang kosong. Yuk, belanja!"

---

## 11. Kotlin 2.x, Material 3, & Compose Best Practices

- **Kotlin 2.x**: Manfaatkan Compose Compiler terintegrasi dan performa K2.
- **Material 3**: Gunakan `MaterialTheme` (colorScheme, typography, shapes) secara konsisten. Hindari warna *hardcoded*.
- **Compose Performance**:
    - Gunakan `@Immutable` atau `@Stable` pada model data UI.
    - Gunakan `remember` untuk komputasi berat di dalam komposabel.
    - Letakkan `Modifier` sebagai parameter pertama (opsional) pada setiap fungsi komposabel.

---

## Alat Bantu Analisis Gemini

Agen disarankan menggunakan:
- `find_usages`: Memahami dampak perubahan.
- `analyze_file`: Memastikan validitas sintaksis.
- `code_search`: Mencari pola implementasi yang sudah ada.

---

## 12. Manajemen Dependensi (Version Catalog)

- **Single Source of Truth**: Semua versi library dan plugin **wajib** dikelola melalui `gradle/libs.versions.toml`.
- **Update Berkala**: Gunakan `version_lookup` atau cek manual untuk memastikan dependensi (seperti Compose BOM, AGP, dan Kotlin) tetap berada di versi stabil terbaru yang kompatibel.
- **Grouping**: Kelompokkan library yang terkait (misal: `androidx-compose-...`) untuk menjaga keterbacaan.

---

## 13. Persistensi Data (Room)

Gunakan Room untuk penyimpanan data terstruktur yang membutuhkan query kompleks atau relasi.

### Aturan Room:
- **Snapshot Historis**: Data riwayat transaksi wajib disimpan sebagai snapshot (salinan nilai), bukan referensi ke tabel master yang bisa berubah.
- **Relasi**: Gunakan `@Relation` untuk menggabungkan entitas (misal: Transaksi dengan ItemTransaksi).
- **Asinkron**: DAO wajib mengembalikan `Flow` untuk pembacaan data dan menggunakan `suspend` untuk penulisan data.
- **Eksport Skema**: Selalu set `exportSchema = true` dan tentukan `room.schemaLocation` di Gradle untuk pelacakan migrasi.
- **Mappers**: Buat fungsi pemetaan (mappers) antara entitas lokal dan model domain di dalam paket `pemetaan`.

---

## 14. Networking & API (Retrofit)

Gunakan Retrofit + OkHttp untuk komunikasi dengan server.

### Aturan Networking:
- **Separation of Concerns**: Jangan gunakan model respons jaringan (DTO) langsung di UI. Selalu petakan (map) ke model domain.
- **Naming DTO**: Gunakan akhiran `ResponsJaringan` (misal: `ResponsProdukJaringan`).
- **Serialisasi**: Gunakan Kotlinx Serialization. Gunakan `@SerialName` jika nama field JSON berbeda dengan nama properti Kotlin (misal: `snake_case` ke `camelCase`).
- **Layanan API**: Simpan interface Retrofit di dalam paket `layanan`.
- **Konfigurasi**: Pusatkan Base URL dan timeout di dalam paket `konfigurasi`.
- **Logging**: Aktifkan `HttpLoggingInterceptor` (Level.BODY) hanya untuk build `DEBUG`.
- **Error Handling**: Tangani exception jaringan (seperti `IOException` atau `HttpException`) di lapisan repository atau use case, bukan di UI.

---
*Dokumen ini merupakan kontrak hidup yang diperbarui seiring perkembangan kapabilitas tim dan teknologi.*
