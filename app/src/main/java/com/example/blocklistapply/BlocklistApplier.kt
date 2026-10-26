package com.example.blocklistapply

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings

object BlocklistApplier {

    private const val KEY = "pubkey_blacklist"

    // SHA-1 of the SubjectPublicKeyInfo of each compromised eMudhra intermediate
    // (impacted-intermediates.json at emudhra.we-have-a-problem.com). Cross-signs share keys.
    val HASHES = linkedMapOf(
        "21ba2f50e6811f19f00918e31e449c50683bee2e" to "EM OV CS CA - G3C",
        "3145de03c55164ac86342b924f45862b5f645969" to "EM EV TLS CA - G3C",
        "385698c49f182af28d29231fabb11f76bf32675c" to "EM OV CS CA - G3B",
        "61d55d09fba074c672c09461b89dddd079ee4abf" to "EM DV TLS CA - G3C",
        "63eeebbaed8e3592d2c87d40eb320edf75ea6494" to "EM EV TLS CA - G3B",
        "b1a1d1dd01491b94c2a71e60033f6d5fe9d72fdf" to "EM DV TLS CA - G3B",
        "c680d2f20aa19fc39379296fa5010dc39f8d0709" to "EM OV TLS CA - G3B",
        "ea337b1e0176d3e21e1296fb988239371312536b" to "EM OV TLS CA - G3C",
    )

    sealed class Result {
        object NoPermission : Result()
        object AlreadyPresent : Result()
        data class Added(val count: Int) : Result()
        data class Failed(val reason: String) : Result()
    }

    fun hasPermission(ctx: Context) =
        ctx.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED

    fun current(ctx: Context): List<String> =
        Settings.Secure.getString(ctx.contentResolver, KEY).orEmpty()
            .split(',').map { it.trim().lowercase() }.filter { it.isNotEmpty() }

    fun missing(ctx: Context) = HASHES.keys - current(ctx).toSet()

    // Merges rather than replaces, so entries already on the device are kept.
    fun apply(ctx: Context): Result {
        if (!hasPermission(ctx)) return Result.NoPermission
        val have = current(ctx)
        val toAdd = HASHES.keys - have.toSet()
        if (toAdd.isEmpty()) return Result.AlreadyPresent
        return try {
            val ok = Settings.Secure.putString(ctx.contentResolver, KEY, (have + toAdd).joinToString(","))
            if (ok) Result.Added(toAdd.size) else Result.Failed("putString returned false")
        } catch (e: SecurityException) {
            Result.Failed(e.message ?: "SecurityException")
        }
    }
}
