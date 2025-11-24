package com.example.mavpulse.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

//this file handles keys
class CryptoManager {

    // Creates instance of keystore to store keys
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    private val aesCipher = Cipher.getInstance("AES/GCM/NoPadding")

    fun getOrCreateAsymmetricKeyPair(alias: String): KeyPair {
        return if (keyStore.containsAlias(alias)) {
            val privateKey = keyStore.getKey(alias, null) as PrivateKey
            val publicKey = keyStore.getCertificate(alias).publicKey
            KeyPair(publicKey, privateKey)
        } else {
            generateAsymmetricKeyPair(alias)
        }
    }

    private fun generateAsymmetricKeyPair(alias: String): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore"
        )

        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setKeySize(2048)
         .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
         .build()

        keyPairGenerator.initialize(spec)
        return keyPairGenerator.generateKeyPair()
    }

    fun generateSymmetricKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }

    fun encryptWithPublicKey(data: ByteArray, publicKey: PublicKey): ByteArray {
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return rsaCipher.doFinal(data)
    }

    fun decryptWithPrivateKey(alias: String, encryptedData: ByteArray): ByteArray {
        val privateKey = keyStore.getKey(alias, null) as PrivateKey
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)
        return rsaCipher.doFinal(encryptedData)
    }
}
