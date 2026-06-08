package com.example.lockit.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * One-way hashing for the master passkey (PBKDF2 + random salt).
 *
 * The encoded form is self-describing so old hashes keep verifying after parameter changes:
 *
 *     pbkdf2$<algoTag>$<iterations>$<saltB64>$<hashB64>
 *
 * Legacy plaintext passkeys (created before hashing existed) are detected by the absence of
 * the `pbkdf2$` prefix; [verify] then compares directly and [needsUpgrade] reports `true`
 * so the caller can re-store a proper hash.
 */
object PasswordHasher {
    private const val PREFIX = "pbkdf2"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256
    private const val SALT_BYTES = 16

    private const val ALGO_SHA256 = "PBKDF2WithHmacSHA256" // API 26+
    private const val ALGO_SHA1 = "PBKDF2WithHmacSHA1"     // available everywhere

    fun hash(passkey: String): String {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val (tag, algo) = preferredAlgorithm()
        val hash = pbkdf2(passkey, salt, ITERATIONS, algo)
        return listOf(PREFIX, tag, ITERATIONS.toString(), b64(salt), b64(hash)).joinToString("$")
    }

    fun verify(passkey: String, stored: String): Boolean {
        if (!isHashed(stored)) return stored == passkey // legacy plaintext
        return try {
            val parts = stored.split("$")
            val algo = algoFor(parts[1])
            val iterations = parts[2].toInt()
            val salt = b64d(parts[3])
            val expected = b64d(parts[4])
            val actual = pbkdf2(passkey, salt, iterations, algo)
            MessageDigest.isEqual(actual, expected)
        } catch (e: Exception) {
            false
        }
    }

    fun isHashed(stored: String): Boolean = stored.startsWith("$PREFIX$")

    /** Plaintext (or otherwise non-hashed) values should be re-stored as a hash. */
    fun needsUpgrade(stored: String): Boolean = !isHashed(stored)

    private fun preferredAlgorithm(): Pair<String, String> = try {
        SecretKeyFactory.getInstance(ALGO_SHA256)
        "sha256" to ALGO_SHA256
    } catch (e: Exception) {
        "sha1" to ALGO_SHA1
    }

    private fun algoFor(tag: String): String = if (tag == "sha256") ALGO_SHA256 else ALGO_SHA1

    private fun pbkdf2(passkey: String, salt: ByteArray, iterations: Int, algo: String): ByteArray {
        val spec = PBEKeySpec(passkey.toCharArray(), salt, iterations, KEY_LENGTH)
        return SecretKeyFactory.getInstance(algo).generateSecret(spec).encoded
    }

    private fun b64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun b64d(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP)
}
