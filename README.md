# Cassy Kasir

Sistem Point of Sale (POS) modern untuk UMKM Indonesia, dibangun dengan prinsip Unidirectional Data Flow (UDF) yang atomik dan antarmuka Jetpack Compose yang adaptif.

## Fokus Proyek

- Kemandirian Bahasa: 100% dokumentasi dan sintaks buatan sendiri menggunakan Bahasa Indonesia yang baku dan teknis.
- Integritas Data: Logika keuangan yang presisi dengan penanganan pajak yang akurat.
- Performa Responsif: Manajemen status asinkron untuk mencegah hambatan pada antarmuka pengguna (UI).

## Arsitektur Sistem

Proyek ini mengadopsi pola MVI-Lite dengan aliran data searah:
1. Status (State): Menggunakan StateFlow tunggal yang tidak dapat diubah (immutable) sebagai sumber kebenaran.
2. Aksi (Action): Interaksi pengguna dikirim sebagai objek sealed interface ke ViewModel.
3. Reduser: ViewModel memperbarui status secara atomik menggunakan fungsi .update untuk menjamin keamanan konkurensi.

## Tumpukan Teknologi

- Kotlin 2.3+: Memanfaatkan fitur terbaru untuk keamanan tipe data.
- Jetpack Compose: UI deklaratif yang mendukung tata letak Ponsel & Tablet.
- Coroutines & Flow: Penanganan proses latar belakang yang efisien.
- Material 3: Desain antarmuka modern dengan dukungan tema dinamis.

## Pedoman Pengembangan (Agentic Docs)

Untuk agen AI atau pengembang baru, harap perhatikan aturan berikut:
- Penamaan: Gunakan kata kerja Bahasa Indonesia untuk fungsi (misal: hitungTotal, bukan calculateTotal).
- KDocs: Setiap fungsi publik wajib memiliki dokumentasi dengan format @param dan @return dalam Bahasa Indonesia.
- UI Stateless: Komposabel tidak boleh menyimpan status internal yang krusial; semua status harus berasal dari parameter.
- AI-Ready: Rujuk GEMINI.md untuk panduan kolaborasi mendalam dengan asisten cerdas.

## Memulai

```powershell
# Bersihkan dan Bangun Proyek
./gradlew clean assembleDebug

# Jalankan Unit Test
./gradlew test
```

---
Dikembangkan untuk kemajuan UMKM Indonesia.
