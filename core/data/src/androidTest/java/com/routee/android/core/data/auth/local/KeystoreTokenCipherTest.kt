package com.routee.android.core.data.auth.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeystoreTokenCipherTest {

    private val cipher = KeystoreTokenCipher()

    /**
     * GIVEN: 토큰 원문이 있을 때
     * WHEN: 암호화한 뒤 복호화하면
     * THEN: 원문으로 돌아온다
     */
    @Test
    fun encryptThenDecryptReturnsPlainText() {
        val encrypted = cipher.encrypt(TOKEN)

        assertNotEquals(TOKEN, encrypted)
        assertEquals(TOKEN, cipher.decrypt(encrypted))
    }

    /**
     * GIVEN: 같은 원문일 때
     * WHEN: 두 번 암호화하면
     * THEN: IV가 달라 결과가 다르다
     */
    @Test
    fun sameTextEncryptsDifferently() {
        assertNotEquals(cipher.encrypt(TOKEN), cipher.encrypt(TOKEN))
    }

    private companion object {
        const val TOKEN = "eyJhbGciOiJIUzI1NiJ9.test.token"
    }
}
