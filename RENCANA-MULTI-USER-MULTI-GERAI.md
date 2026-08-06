# Rencana: Multi-User, Multi-Gerai, dan Sinkronisasi ke Website

> Dokumen konteks perencanaan. Tanggal: 03 Agustus 2026.
> Model acuan: Qasir.id — setiap UMKM bisa daftar akun, 1 akun punya multi gerai,
> akun utama bisa menambah akun lain (mis. kasir) dan meng-assign ke gerai tertentu.

## 1. Tujuan

- Aplikasi Android `flexi-chasier` bisa dipakai **multi-user** dan **multi-gerai**.
- Data dari Android **tersinkron 2 arah** ke backend/website.
- Pemilik (owner) bisa lihat **grafik, laporan, rekap** dari website.
- Semua role (Pemilik & Kasir) bisa login ke **website** dan **aplikasi Android**.

## 2. Keputusan yang sudah disepakati

| Keputusan | Pilihan |
| --- | --- |
| Backend | Belum ada — dibuat dari nol: **Node.js + Express + Prisma + PostgreSQL** |
| Scope sinkronisasi MVP | **Semua entitas sekaligus** (transaksi, kas, bahan, dll.) |
| Model multi-gerai | **Satu DB lokal + kolom `geraiId`** |
| Model auth | **Username + password** (JWT) |
| Akses website | **Semua role** bisa login; owner lihat semua gerai, kasir hanya gerai ter-assign |

## 3. Arsitektur keseluruhan

```
Tenant (UMKM, 1 akun utama — milik owner)
 ├─ Gerai A ──┐
 ├─ Gerai B ──┤
 └─ Gerai C ──┘
User (akun login, milik tenant)
 ├─ Pemilik → akses semua gerai
 └─ Kasir   → di-assign ke gerai tertentu (tabel user_gerai)

┌─ Android (local-first) ─┐        ┌──────── Backend Node.js ─────────┐        ┌─ Website ─┐
│ Room kasir.db           │ REST    │ Express + PostgreSQL + Prisma   │        │ Dashboard │
│  └ outbox (antrean sync)├───────►│  /api/auth  /api/sync/*  /api/…  │◄──────┤ (grafik,  │
│  └ Flow untuk UI        │  JWT   │  isolasi data per tenant_id      │        │ laporan)  │
└─────────────────────────┘        └──────────────────────────────────┘        └───────────┘
```

- Android **local-first**: UI selalu baca `Flow` dari Room, tidak berubah dengan adanya sync.
- Semua data server diisolasi per **`tenant_id`** (poin keamanan utama karena SaaS multi-tenant).
- Semua baris punya **`versi`** (BIGINT) untuk konflik **last-write-wins**.

## 4. Backend (repo baru: `flexi-chasier-server`)

### 4.1 Stack
- Node.js + Express (REST JSON, format snake_case)
- Prisma ORM + PostgreSQL
- JWT (`jsonwebtoken`), validasi zod
- docker-compose untuk lokal

### 4.2 Skema Postgres

Semua tabel data memiliki:
`id UUID`, `tenant_id`, `gerai_id`, `versi BIGINT`, `dibuat/diubah TIMESTAMP`, soft-delete.

Tabel master & relasi:
- `tenants` — UMKM (nama, pemilik)
- `users` — akun login, `tenant_id`, peran (`Pemilik` / `Kasir`), aktif
- `gerai` — cabang, `tenant_id`
- `user_gerai` — relasi user ⇄ gerai (kasir hanya melihat gerai ter-assign)

Tabel data (semua punya `tenant_id` + `gerai_id`):
- `products`
- `transactions` + `transaction_items`
- `tables` (meja)
- `cash_shifts` + `cash_mutations` + `setoran`
- `bahan` + `pembelian_bahan` + `resep` + `resep_bahan`
- `store_settings` (nama usaha, alamat, tagline, logo per gerai)
- `sync_cursors` (metadata sinkronisasi per entitas/gerai)

### 4.3 Endpoint

**Auth**
- `POST /api/auth/register` — buat tenant + user Pemilik + gerai pertama
- `POST /api/auth/login` — response: accessToken, refreshToken, user, daftar gerai
- `POST /api/auth/refresh` — rotasi: token lama dicabut (`revokedAt`), token baru diterbitkan
- `POST /api/auth/logout` — mencabut refresh token di server (idempoten, best-effort)
- Refresh token disimpan hash (SHA-256) di tabel `RefreshToken`; reset password mencabut semua sesi

**Gerai & user**
- `GET/POST /api/gerai`, `PUT /api/gerai/:id`
- `GET/POST /api/gerai/:id/users` — owner menambah akun kasir + assign gerai

**Sinkronisasi (push — bulk, idempotent upsert-by-id, tiap item ber-`versi`)**
- `POST /api/sync/produk`
- `POST /api/sync/transaksi`
- `POST /api/sync/transaksi/:id/items`
- `POST /api/sync/meja`
- `POST /api/sync/shift-kas`
- `POST /api/sync/mutasi-kas`
- `POST /api/sync/setoran`
- `POST /api/sync/bahan`
- `POST /api/sync/pembelian-bahan`
- `POST /api/sync/resep`
- `POST /api/sync/pengaturan-toko`

**Pull**
- `GET /api/sync/perubahan?geraiId=&batas=&<entitas>=<kursor>` → daftar baris ber-`versi`
- Kursor per entitas berformat keyset `"<epochMili>:<id>"` (`"0:"` = dari awal), digabung dengan `waktuDiubah` + `id` agar gap-free tanpa melewatkan baris ber-timestamp sama. Respons mengembalikan `kursorBaru` per entitas (maju hanya sejauh baris yang benar-benar dikirim) dan `terpotong`; klien menarik ulang selama `terpotong` true.
- Item transaksi & bahan resep dikirim lengkap per induk (tidak dipaginasikan sendiri) agar tidak ada item yang hilang saat batch induk terpotong.

**Laporan (untuk website)**
- `GET /api/laporan/penjualan-harian?geraiId=&dari=&sampai=`
- `GET /api/laporan/penjualan-periode`
- `GET /api/laporan/rekap-kas`
- `GET /api/laporan/produk-terlaris`
- `GET /api/laporan/stok`
- `GET /api/laporan/mutasi`

### 4.4 Catatan keamanan
- Isolasi `tenant_id` WAJIB di setiap query server (dari token JWT, jangan dari payload).
- `user_gerai` membatasi akses kasir pada gerai tertentu.

## 5. Android — Perubahan

### 5.1 Auth & multi-user
- Migrasi Room **`DARI_22_KE_23`**: tabel `user`, `gerai`, `outbox`; kolom `geraiId`, `dibuatOleh`, `versi` (default 0) di semua tabel syncable.
- **LoginScreen** sebagai start destination (NavHost sekarang start di Dashboard).
- Token disimpan di **EncryptedSharedPreferences (Keystore)**.
- **AuthInterceptor** di OkHttp: attach Bearer, handle 401 → refresh → retry.
- **Role-gate menu**: Kasir tanpa akses KelolaProduk/Kas/Pengaturan (sidebar sudah mendukung item kondisional).
- `shift_kas` & transaksi mencatat `dibuatOleh` (operator — sekarang shift tanpa operator).

### 5.2 Multi-gerai
- Satu `kasir.db`, tiap baris diberi `geraiId`; pilih gerai aktif saat login/switch.
- `StoreSetting`/`StorePreference` DataStore di-partisi per `geraiId`.
- **`ProductIdGenerator` → UUID** (sekarang `produk-<slug>-6char`, rawan tabrakan lintas perangkat). Transaksi sudah UUID.
- Query rekap/laporan tambah varian `WHERE geraiId = ?`.

### 5.3 Mesin sinkronisasi (outbox)
- **Write path:** semua repositori mutasi menulis ke `outbox` **dalam transaksi Room yang sama** (seam-nya sudah ada: `simpanTransactionDenganDeltaStok` memakai `@Transaction`).
- **Push:** `SyncWorker` (WorkManager, constraint `CONNECTED`) mengosongkan outbox → POST per entitas → tandai Dikirim; retry backoff, cap percobaan → status Gagal.
- **Pull:** generalisasi `sinkronkanKatalog()` (pola `ProductRepositoryLokalRemote`) jadi `ambilPerubahanSejak()` untuk semua entitas; server menang untuk katalog, `favorit` lokal dipertahankan.
- **Konflik:** `versi` last-write-wins.
- UI: status sync + tombol "Sinkronkan sekarang" di Pengaturan (pakai `SyncStatus` yang sudah ada).

## 6. Website (dashboard)

- Web app (React/Next.js) baca API yang sama; login semua role.
- Owner lihat semua gerai; kasir hanya gerai ter-assign.
- Halaman: ringkasan (total penjualan, transaksi, kas), **grafik penjualan** (baris/line harian-bulanan), **laporan** (transaksi, rekap kas, setoran, stok, produk terlaris), filter per gerai & rentang tanggal, ekspor PDF/CSV.

## 7. Tahapan implementasi

1. **Phase 0 — Stabilisasi repo:** commit hasil rename `id.flexi.kasir` (working tree belum di-commit sama sekali).
2. **Phase 1 — Backend inti:** scaffold Node + Prisma + skema (tenants/users/gerai/user_gerai) + auth + endpoint sync/laporan.
3. **Phase 2 — Auth Android:** migrasi Room 23, layar login, Keystore, interceptor, role-gate.
4. **Phase 3 — Outbox & sync:** outbox di semua repositori, SyncWorker, UUID produk, kolom `geraiId`.
5. **Phase 4 — Website:** dashboard grafik & laporan, akses per role.
6. **Phase 5 — Sinkronisasi lengkap & hardening:** kas/bahan penuh, konflik, migrasi production, testing.

## 8. Risiko yang teridentifikasi

- **ID produk bentrok** antar perangkat → wajib ganti ke UUID sebelum multi-device.
- **`StoreSetting` di DataStore**, bukan Room → butuh endpoint `pengaturan-toko` khusus.
- **Belum ada WorkManager** di dependensi → perlu ditambahkan.
- **Isolasi tenant** adalah keamanan inti SaaS — setiap query server wajib difilter `tenant_id`.
- Scope "semua entitas sekaligus" membuat Phase 3–5 besar — bisa dipangkas jika MVP mendesak.

## 9. Referensi file yang relevan (Android)

- `app/src/main/java/id/flexi/kasir/CashierDependencyContainer.kt` — DI manual; titik pasang repositori hybrid & auth.
- `app/src/main/java/id/flexi/kasir/data/local/database/FlexiCashierDatabase.kt` — Room, version 22.
- `app/src/main/java/id/flexi/kasir/data/local/database/CashierDatabaseMigration.kt` — rantai migrasi 1→22; tambah DARI_22_KE_23.
- `app/src/main/java/id/flexi/kasir/data/repository/ProductRepositoryLokalRemote.kt` — pola local-first + sync yang sudah ada.
- `app/src/main/java/id/flexi/kasir/data/network/config/CashierNetworkProvider.kt` — OkHttp/Retrofit; titik pasang AuthInterceptor.
- `app/src/main/java/id/flexi/kasir/domain/identity/ProductIdGenerator.kt` — harus diganti UUID.
- `app/src/main/java/id/flexi/kasir/ui/navigation/FlexiCashierNavigation.kt` — start destination; ganti jadi Login.
- `app/build.gradle.kts` + `gradle/libs.versions.toml` — tambah WorkManager, Keystore/EncryptedSharedPreferences, dll.
