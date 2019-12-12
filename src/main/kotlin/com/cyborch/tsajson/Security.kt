package com.cyborch.tsajson

import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64.getDecoder

/**
 * Sign plaintext using a private key.
 *
 * @param plainText The text to be signed.
 *
 * @param privateKey A private key used to sign the text.
 *
 * @return ByteArray with a signature generated using SHA256withRSA algorithm.
 */
@Throws(Exception::class)
fun sign(plainText: String, privateKey: PrivateKey?): ByteArray {
    val privateSignature: Signature = Signature.getInstance("SHA256withRSA")
    privateSignature.initSign(privateKey)
    privateSignature.update(plainText.toByteArray(charset("UTF-8")))
    val signature: ByteArray = privateSignature.sign()
    return signature
}

/**
 * Load private key from a predefined location.
 *
 * It is a prerequisite that the create_tsa_certs script is used to generate
 * keys and store them in the correct location.
 *
 * @return A PrivateKey instance.
 */
fun loadPrivateKey(): PrivateKey {
    var privateKeyContent = String(Files.readAllBytes(Paths.get("/var/lib/tsa/tsa_key1.pem")))
    privateKeyContent = privateKeyContent
        .replace("\\n".toRegex(), "")
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
    val kf = KeyFactory.getInstance("RSA")
    val keySpecPKCS8 = PKCS8EncodedKeySpec(getDecoder().decode(privateKeyContent))
    val privKey = kf.generatePrivate(keySpecPKCS8)

    return privKey
}

/**
 * The PEM formatted public key.
 *
 * It is a prerequisite that the create_tsa_certs script is used to generate
 * keys and store them in the correct location.
 *
 * @return a String containing the PEM formatted public key.
 */
fun publicKeyContent(): String = String(Files.readAllBytes(Paths.get("/var/lib/tsa/tsa_pub1.pem")))

/**
 * The PEM formatted certificate.
 *
 * It is a prerequisite that the create_tsa_certs script is used to generate
 * keys and store them in the correct location.
 *
 * @return a String containing the PEM formatted certificate.
 */
fun certContent(): String = String(Files.readAllBytes(Paths.get("/var/lib/tsa/tsa_cert1.pem")))
