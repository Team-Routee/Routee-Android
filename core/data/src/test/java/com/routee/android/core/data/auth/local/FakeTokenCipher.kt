package com.routee.android.core.data.auth.local

import java.security.GeneralSecurityException

internal class FakeTokenCipher : TokenCipher {
    override fun encrypt(plainText: String): String = PREFIX + plainText.reversed()

    override fun decrypt(cipherText: String): String {
        if (!cipherText.startsWith(PREFIX)) throw GeneralSecurityException()
        return cipherText.removePrefix(PREFIX).reversed()
    }

    private companion object {
        const val PREFIX = "enc:"
    }
}
