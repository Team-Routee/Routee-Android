package com.routee.android.core.data.auth.local

import java.security.GeneralSecurityException

internal class FakeTokenCipher(
    private val decryptException: Exception? = null,
) : TokenCipher {
    override fun encrypt(plainText: String): String = PREFIX + plainText.reversed()

    override fun decrypt(cipherText: String): String {
        decryptException?.let { throw it }
        if (!cipherText.startsWith(PREFIX)) throw GeneralSecurityException()
        return cipherText.removePrefix(PREFIX).reversed()
    }

    private companion object {
        const val PREFIX = "enc:"
    }
}
