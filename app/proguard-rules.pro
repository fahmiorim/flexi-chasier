# ─────────────────────────────────────────────────────────────────────
# ProGuard / R8 Rules — Flexi Kasir
# Release build mengaktifkan isMinifyEnabled=true; file ini WAJIB
# memuat keep rules agar class refleksi / serialisasi tidak dihapus.
# ─────────────────────────────────────────────────────────────────────

# ── Simpan info baris untuk crash log yang berguna ──
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*,InnerClasses,EnclosingMethod,Signature

# ──────────────────────────────────────────────────────────────────────
# SELURUH APLIKASI — keep semua class di namespace id.flexi.kasir
# Approach paling aman: tidak ada class app yang boleh dihapus/diubah.
# ──────────────────────────────────────────────────────────────────────
-keep class id.flexi.kasir.** { *; }
-keep class id.flexi.kasir.** { <init>(...); }

# ──────────────────────────────────────────────────────────────────────
# Kotlin
# ──────────────────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepclassmembers class * {
    @kotlin.jvm.JvmField <fields>;
    @kotlin.jvm.JvmStatic <methods>;
}
-dontwarn kotlin.**

# ──────────────────────────────────────────────────────────────────────
# Kotlin Serialization — keep serializers + companion + type adapters
# ──────────────────────────────────────────────────────────────────────
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class id.flexi.kasir.**$$serializer { *; }
-keepclassmembers class id.flexi.kasir.** {
    *** Companion;
}

# ──────────────────────────────────────────────────────────────────────
# Room Database
# ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase$Callback
-keep class * extends androidx.room.paging.LimitOffsetDataSource

# ──────────────────────────────────────────────────────────────────────
# Retrofit
# ──────────────────────────────────────────────────────────────────────
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# ──────────────────────────────────────────────────────────────────────
# Socket.IO Client
# ──────────────────────────────────────────────────────────────────────
-keep class io.socket.** { *; }
-dontwarn io.socket.**
-keep class io.socket.engineio.** { *; }
-dontwarn io.socket.engineio.**
-keep class io.socket.client.** { *; }
-dontwarn io.socket.client.**

# ──────────────────────────────────────────────────────────────────────
# OkHttp
# ──────────────────────────────────────────────────────────────────────
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn okhttp3.internal.platform.**
-dontwarn org.animalsniffssl.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ──────────────────────────────────────────────────────────────────────
# AndroidX Security Crypto (EncryptedSharedPreferences)
# ──────────────────────────────────────────────────────────────────────
-keep class androidx.security.crypto.** { *; }

# ──────────────────────────────────────────────────────────────────────
# DataStore Preferences
# ──────────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { *; }

# ──────────────────────────────────────────────────────────────────────
# Kotlinx Coroutines
# ──────────────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ──────────────────────────────────────────────────────────────────────
# Paging 3
# ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.paging.PagingSource

# ──────────────────────────────────────────────────────────────────────
# WorkManager
# ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker
-keepclassmembers class * {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ──────────────────────────────────────────────────────────────────────
# Compose Navigation — keep serializable destinations
# ──────────────────────────────────────────────────────────────────────
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ──────────────────────────────────────────────────────────────────────
# R8 full mode compatibility
# ──────────────────────────────────────────────────────────────────────
-allowaccessmodification
-repackageclasses ''
