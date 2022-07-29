package com.evermore.sha256aestest

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.preference.PreferenceManager
import android.util.Base64
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class AESUtil {
    companion object {
        private val TAG = AESUtil::class.simpleName
        private const val encryptionTransformation = "AES/CBC/PKCS7Padding"
        private const val encryptionTransformationNoIV = "AES/EBC/PKCS7Padding"
        fun decrypt(context: Context, data: String): String? {
            try {
                val cipher = Cipher.getInstance(encryptionTransformation)
                val ivStr = getSavedInitializationVectorAsString(context)
                if (ivStr != "no_iv") {
                    val ivSpec = IvParameterSpec(getSavedInitializationVector(context))
                    cipher.init(Cipher.DECRYPT_MODE, getSavedSecretKey(context), ivSpec)
                } else {
                    cipher.init(Cipher.DECRYPT_MODE, getSavedSecretKey(context))
                }
                val byteArray = Base64.decode(data, Base64.NO_WRAP)
                var cipherText = cipher.doFinal(byteArray)
                val decValue = String(cipherText)
                Log.d(TAG, "decValue > $decValue")
                return decValue
            } catch (e: Exception) {
                Log.e(TAG, "decrypt data fail", e)
            }
            return null
        }

        fun encrypt(context: Context, data: String, useSavedKey: Boolean = false, iv: String?): String? {
            try {
                val keygen = KeyGenerator.getInstance("AES")
                keygen.init(256)
                val enckey = if (useSavedKey) {
                    getSavedSecretKey(context)
                } else {
                    val key = keygen.generateKey()
                    saveSecretKey(context, key)
                    key
                }
                val ivParameterSpec = iv?.let { IvParameterSpec(iv.toByteArray(Charset.forName("utf-8"))) }
                val cipher = Cipher.getInstance(encryptionTransformation)
                ivParameterSpec?.let { cipher.init(Cipher.ENCRYPT_MODE, enckey, ivParameterSpec) } ?: cipher.init(Cipher.ENCRYPT_MODE, enckey)
                var cipherText = cipher.doFinal(data.toByteArray(Charset.forName("utf-8")))
                saveInitializationVector(context, cipher.iv)
                val encValue = Base64.encodeToString(cipherText, Base64.NO_WRAP)
                Log.d(TAG, "encValue > $encValue")
                return encValue
            } catch (e: Exception) {
                Log.e(TAG, "encrypt data fail", e)
            }
            return null
        }

        private fun saveSecretKey(context: Context, secretKey: SecretKey) {
            val baos = ByteArrayOutputStream()
            val oos = ObjectOutputStream(baos)
            oos.writeObject(secretKey)
            val strToSave = String(android.util.Base64.encode(baos.toByteArray(), android.util.Base64.DEFAULT))
            val sharedPref = context.getSharedPreferences("aes_test", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("secret_key", strToSave)
            editor.apply()
        }

        private fun getSavedSecretKey(context: Context): SecretKey {
            val sharedPref = context.getSharedPreferences("aes_test", MODE_PRIVATE)
            val strSecretKey = sharedPref.getString("secret_key", "")
            val bytes = android.util.Base64.decode(strSecretKey, android.util.Base64.DEFAULT)
            val ois = ObjectInputStream(ByteArrayInputStream(bytes))
            val secretKey = ois.readObject() as SecretKey
            return secretKey
        }

        private fun saveInitializationVector(context: Context, initializationVector: ByteArray?) {
            Log.d(TAG, "initializationVector > ${initializationVector == null}")
            val strToSave = initializationVector?.let {
                val baos = ByteArrayOutputStream()
                val oos = ObjectOutputStream(baos)
                oos.writeObject(initializationVector)
                String(android.util.Base64.encode(baos.toByteArray(), android.util.Base64.DEFAULT))
            } ?: "no_iv"
            val sharedPref = context.getSharedPreferences("aes_test", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("initialization_vector", strToSave)
            editor.apply()
        }

        private fun getSavedInitializationVector(context: Context): ByteArray {
            val sharedPref = context.getSharedPreferences("aes_test", MODE_PRIVATE)
            val strInitializationVector = sharedPref.getString("initialization_vector", "")
            val bytes = android.util.Base64.decode(strInitializationVector, android.util.Base64.DEFAULT)
            val ois = ObjectInputStream(ByteArrayInputStream(bytes))
            val initializationVector = ois.readObject() as ByteArray
            return initializationVector
        }

        fun getSavedSecretKeyAsString(context: Context): String? {
            val sharedPref = context.getSharedPreferences("aes_test", MODE_PRIVATE)
            return sharedPref.getString("secret_key", "no_key")
        }

        fun getSavedInitializationVectorAsString(context: Context): String? {
            val sharedPref = context.getSharedPreferences("aes_test", MODE_PRIVATE)
            return sharedPref.getString("initialization_vector", "no_iv")
        }

        fun toHex(bytes: ByteArray): String {
            val stringBuilder = StringBuilder()
            for (b in bytes) {
                stringBuilder.append(String.format("%02X", b))
            }
            return stringBuilder.toString()
        }
    }
}