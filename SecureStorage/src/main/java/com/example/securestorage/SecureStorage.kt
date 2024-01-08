package com.example.securestorage


import javax.crypto.Cipher
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore


import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey

import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


import java.nio.charset.Charset
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class DataStoreManager(val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_data")
    private val keyPair: KeyPair
    val secretKey: SecretKey
    init {
        // Generate an RSA key pair
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)


        // You can adjust the key size as needed
        keyPair = keyPairGenerator.generateKeyPair()
        secretKey = generateSymmetricKey()
    }
    private fun generateSymmetricKey(): SecretKey {
        try {
            // Generate a symmetric key (e.g., AES)
            // Note: You may use a secure method to generate the key
            val keyGenerator = javax.crypto.KeyGenerator.getInstance("AES")


            return keyGenerator.generateKey()
        } catch (e: Exception) {
            // Log the exception for debugging purposes
            println(e.toString())
            e.printStackTrace()
            throw e
        }
    }
//    private fun encryptSymmetricKey(secretKey: SecretKey, publicKey: PublicKey): ByteArray {
//        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
//        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
//        return cipher.doFinal(secretKey.encoded)
//    }
//
//    private fun decryptSymmetricKey(encryptedKey: ByteArray, privateKey: PrivateKey): SecretKey {
//        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
//        cipher.init(Cipher.DECRYPT_MODE, privateKey)
//        val decryptedKeyBytes = cipher.doFinal(encryptedKey)
//        return SecretKeySpec(decryptedKeyBytes, "AES")
//    }

    private fun encryptString(data: String, secretKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")


        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        println(data)
        return cipher.doFinal(data.toByteArray())
    }
    private fun decryptString(encryptedData: ByteArray, secretKey: SecretKey): String {
        try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            println("///////")
            val decryptedBytes = cipher.doFinal(encryptedData)
            println("///////")
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // Print debug information
            println("Error during decryption: ${e.message}")
            println("Encrypted data length: ${encryptedData.size}")
            throw e
        }
    }

//    private fun encryptString(input: String, publicKey: PublicKey): ByteArray {
//        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
//        cipher.init(Cipher.ENCRYPT_MODE, publicKey,)
//
//        return cipher.doFinal(input.toByteArray())
//    }
//
//
//    private fun decryptString(encryptedInput: ByteArray, privateKey: PrivateKey): String {
//        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
//        cipher.init(Cipher.DECRYPT_MODE, privateKey)
//        val decryptedBytes = cipher.doFinal(encryptedInput)
//        return String(decryptedBytes)
//    }

    private fun getPublicKey(): PublicKey {
        return keyPair.public
    }

    // Get the private key for decryption
    private fun getPrivateKey(): PrivateKey {
        return keyPair.private
    }
//    private fun stringToByteArray(input: String): ByteArray {
//        return input.toByteArray(Charsets.UTF_8) // Specify the character set (UTF-8 in this example)
//    }
//    private fun byteArrayToString(input: ByteArray, charset: Charset = Charsets.UTF_8): String {
//        return String(input, charset)
//    }
      fun saveData(key: String, value: String) {

          // Encrypt the actual data with the symmetric key
          val encryptedData = encryptString(value, secretKey)

println(encryptedData)

        runBlocking {
            launch(Dispatchers.IO) {
                context.dataStore.edit { preferences ->
                    preferences[stringPreferencesKey(key)] = encryptedData.toString()
                }
            }
        }
    }

    fun readData(key: String):String? {
//        val encryptedSymmetricKey = encryptSymmetricKey(secretKey, getPublicKey())
//
//        val decryptedSymmetricKey = decryptSymmetricKey(encryptedSymmetricKey, getPrivateKey())
//

        // Decrypt the actual data with the symmetric key


        return runBlocking {
            withContext(Dispatchers.IO) {
                val data=context.dataStore.data.first()[stringPreferencesKey(key)]
println(data)
                val decryptedData = decryptString(data!!.toByteArray(), secretKey)
              decryptedData


            }
        };
    }
}