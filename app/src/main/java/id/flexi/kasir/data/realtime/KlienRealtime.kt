package id.flexi.kasir.data.realtime

import android.content.Context
import android.util.Log
import id.flexi.kasir.data.auth.TokenStore
import id.flexi.kasir.data.network.config.CashierNetworkConfig
import id.flexi.kasir.data.sync.SinkronisasiPenjadwal
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject

/**
 * Klien Socket.IO untuk menerima push real-time dari server.
 *
 * Saat server mengirim event [EVENT_PERUBAHAN] (entitas berubah),
 * klien langsung memicu sinkronisasi via [SinkronisasiPenjadwal].
 * Ini menggantikan ketergantungan pada polling periodik 15 menit.
 *
 * @param tokenStore Menyediakan JWT token untuk autentikasi WebSocket.
 * @param konteks Konteks aplikasi untuk menjalankan WorkManager sync.
 */
class KlienRealtime(
    private val tokenStore: TokenStore,
    private val konteks: Context,
) {

    private var socket: Socket? = null

    /**
     * Menghubungkan ke server Socket.IO.
     * Dipanggil setelah login atau saat aplikasi dibuka dengan sesi aktif.
     * Tidak akan membuat duplikat koneksi jika sudah terhubung.
     */
    fun hubungkan() {
        if (socket?.connected() == true) return

        val token = tokenStore.aksesToken
        if (token.isNullOrBlank()) {
            Log.w(TAG, "Tidak ada token — lewatkan koneksi realtime")
            return
        }

        val alamatServer = alamatServerRealtime()
        Log.d(TAG, "Menghubungkan ke $alamatServer")

        try {
            val options = IO.Options.builder()
                .setAuth(mapOf("token" to token))
                .setReconnection(true)
                .setReconnectionAttempts(JUMLAH_CUBA_ULANG)
                .setReconnectionDelay(DELAY_CUBA_ULANG_MS.toLong())
                .setReconnectionDelayMax(DELAY_MAKS_CUBA_ULANG_MS.toLong())
                .setTimeout(TIMEOUT_MS.toLong())
                .build()

            socket = IO.socket(alamatServer, options)

            socket?.on(Socket.EVENT_CONNECT, onConnect)
            socket?.on(Socket.EVENT_DISCONNECT, onDisconnect)
            socket?.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
            socket?.on(EVENT_PERUBAHAN, onPerubahan)

            socket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Gagal membuat koneksi Socket.IO", e)
        }
    }

    /**
     * Memutuskan koneksi Socket.IO.
     * Dipanggil saat logout atau aplikasi dihancurkan.
     */
    fun putuskan() {
        socket?.off()
        socket?.disconnect()
        socket = null
        Log.d(TAG, "Koneksi realtime diputuskan")
    }

    /**
     * Menghubungkan ulang dengan token baru.
     * Dipanggil setelah refresh token.
     */
    fun sambungUlang() {
        putuskan()
        hubungkan()
    }

    /** Status koneksi saat ini. */
    val terhubung: Boolean
        get() = socket?.connected() == true

    // ── Listener Events ──

    private val onConnect = Emitter.Listener {
        Log.d(TAG, "Terhubung ke server real-time (id=${socket?.id()})")
        // Sinkronisasi data saat pertama kali terhubung / reconnect
        SinkronisasiPenjadwal.mintaSinkronisasiSekarang(konteks)
    }

    private val onDisconnect = Emitter.Listener { args ->
        Log.w(TAG, "Terputus dari server real-time: ${args.firstOrNull()}")
    }

    private val onConnectError = Emitter.Listener { args ->
        val error = args.firstOrNull()
        Log.e(TAG, "Gagal koneksi real-time: $error")
    }

    private val onPerubahan = Emitter.Listener { args ->
        try {
            val payload = args.firstOrNull() as? JSONObject ?: return@Listener
            val entitas = payload.optString("entitas", "?")
            Log.d(TAG, "Perubahan diterima: $entitas — picu sinkronisasi")

            SinkronisasiPenjadwal.mintaSinkronisasiSekarang(konteks)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memproses event perubahan", e)
        }
    }

    // ── Utilitas ──

    private fun alamatServerRealtime(): String {
        return CashierNetworkConfig.alamatDasarApi.trimEnd('/')
    }

    companion object {
        private const val TAG = "KlienRealtime"
        private const val EVENT_PERUBAHAN = "perubahan"
        private const val JUMLAH_CUBA_ULANG = -1  // tak terbatas
        private const val DELAY_CUBA_ULANG_MS = 3000
        private const val DELAY_MAKS_CUBA_ULANG_MS = 30_000
        private const val TIMEOUT_MS = 10_000
    }
}
