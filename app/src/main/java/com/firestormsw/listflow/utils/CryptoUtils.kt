package com.firestormsw.listflow.utils

import android.util.Log
import okio.ByteString.Companion.decodeHex
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.Provider
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class CryptoUtils {
    @OptIn(ExperimentalStdlibApi::class)
    companion object {
        private const val ECDH_ALGORITHM = "ECDH"
        private const val EC_CURVE = "secp256r1"
        private const val AES_GCM_ALGORITHM = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12

        private val provider: Provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)

        init {
            if (provider.javaClass != BouncyCastleProvider::class.java) {
                Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
                Security.insertProviderAt(BouncyCastleProvider(), 1)
            }
            Log.w("TinyList", "Added BouncyCastle provider")
        }

        fun generateECDHKeyPair(): KeyPair {
            val keyPairGenerator = KeyPairGenerator.getInstance(ECDH_ALGORITHM, "BC")
            val ecSpec = ECNamedCurveTable.getParameterSpec(EC_CURVE)
            keyPairGenerator.initialize(ecSpec, SecureRandom())
            return keyPairGenerator.generateKeyPair()
        }

        fun deriveSharedSecret(privateKey: PrivateKey, peerPublicKey: PublicKey): ByteArray {
            val keyAgreement = KeyAgreement.getInstance(ECDH_ALGORITHM)
            keyAgreement.init(privateKey)
            keyAgreement.doPhase(peerPublicKey, true)
            return keyAgreement.generateSecret()
        }

        fun decodePublicKey(encodedPublicKey: String): PublicKey {
            val publicKeyBytes = encodedPublicKey.decodeHex().toByteArray()
            val keyFactory = KeyFactory.getInstance(ECDH_ALGORITHM, "BC")
            val keySpec = X509EncodedKeySpec(publicKeyBytes)
            return keyFactory.generatePublic(keySpec)
        }

        fun deriveAESKey(sharedSecret: ByteArray): SecretKey {
            val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val keySpec = PBEKeySpec(
                sharedSecret.toHexString(HexFormat.UpperCase).toCharArray(),
                sharedSecret,
                10000,
                256
            )
            val keyBytes = keyFactory.generateSecret(keySpec).encoded
            return SecretKeySpec(keyBytes, "AES")
        }

        fun encryptData(data: ByteArray, secretKey: SecretKey): ByteArray {
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)

            val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
            val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

            val encryptedBytes = cipher.doFinal(data)

            return encryptedBytes + iv
        }

        fun decryptData(data: ByteArray, secretKey: SecretKey): ByteArray {
            val iv = data.copyOfRange(data.size - GCM_IV_LENGTH, data.size)

            val cipher = Cipher.getInstance(AES_GCM_ALGORITHM)
            val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

            return cipher.doFinal(data.copyOfRange(0, data.size - GCM_IV_LENGTH))
        }
    }
}