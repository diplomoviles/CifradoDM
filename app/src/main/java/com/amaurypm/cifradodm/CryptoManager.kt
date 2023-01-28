package com.amaurypm.cifradodm

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * Creado por Amaury Perea Matsumura el 28/01/23
 */
class CryptoManager {
    //creando una instancia al keystore (llavero)
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    companion object{
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES //Algoritmo simétrico para encriptar
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC // Estableciendo la forma de encriptación
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }

    //Creando la llave para encriptar/desencriptar
    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder(
                    "my_secret_key",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    //Para obtener la llave. Si ya existe, se regresa la que se tiene. Si no, se crea una nueva
    private fun getKey(): SecretKey{
        val existingKey = keyStore.getEntry("my_secret_key", null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    //Iniando el cifrador (como una variable)
    private val encryptCipher = Cipher.getInstance(TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, getKey())
    }

    //Se requiere un vector de inicialización (ese vector se crea cuando se encripta)
    private fun getDecryptionCipherForIv(iv: ByteArray): Cipher{
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))  //Le pasamos el vector de inicialización
        }
    }


    fun encrypt(bytes: ByteArray, outputStream: OutputStream): ByteArray{
        val encryptedBytes = encryptCipher.doFinal(bytes) //este ya daría el byte array encriptado
        //agregando el vector de inicialización a lo encriptado, para que después se pueda desencriptar:
        outputStream.use{ outputStream ->
            outputStream.write(encryptCipher.iv.size)  //escribe el tamaño del iv que se usó
            outputStream.write(encryptCipher.iv) //escribimos el iv como tal
            outputStream.write(encryptedBytes.size) //tamaño de lo que encriptamos
            outputStream.write(encryptedBytes) //escribimos lo encriptado
        }
        return encryptedBytes
    }

    fun decrypt(inputStream: InputStream): ByteArray{
        return inputStream.use { inputStream ->
            val ivSize = inputStream.read()
            val iv = ByteArray(ivSize)
            inputStream.read(iv)

            val encryptedBytesSize = inputStream.read()
            val encryptedBytes = ByteArray(encryptedBytesSize)
            inputStream.read(encryptedBytes)

            getDecryptionCipherForIv(iv).doFinal(encryptedBytes)
        }
    }
}