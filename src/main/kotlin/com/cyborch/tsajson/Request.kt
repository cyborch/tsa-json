package com.cyborch.tsajson

import java.math.BigInteger

/***
 * Message imprint as seen in RFC 3161 section 2.4.1.
 *
 * @property hashAlgorithm The hash algorithm indicated in the hashAlgorithm
 * field SHOULD be a known hash algorithm (one-way and collision resistant).
 * That means that it SHOULD be one-way and collision resistant.  The Time Stamp
 * Authority SHOULD check whether or not the given hash algorithm is
 * known to be "sufficient" (based on the current state of knowledge in
 * cryptanalysis and the current state of the art in computational
 * resources, for example).  If the TSA does not recognize the hash
 * algorithm or knows that the hash algorithm is weak (a decision left
 * to the discretion of each individual TSA), then the TSA SHOULD refuse
 * to provide the time-stamp token by returning a pkiStatusInfo of
 * 'bad_alg'.
 *
 * @property hashedMessage
 * The hash is represented as a hex string. Its
 * length MUST match the length of the hash value for that algorithm
 * (e.g., 40 hex characters for SHA-1).
 */
data class MessageImprint(
    val hashAlgorithm: String,
    val hashedMessage: String
) {
    fun supportedAlgorithms() = arrayOf("sha1", "sha256")
    fun lengthOfMessage(forAlgorithm: String) = mapOf<String, Int>(
        "sha1" to 40,
        "sha256" to 64
    )[forAlgorithm]
    fun valid() =
        supportedAlgorithms().indexOf(hashAlgorithm) != -1 &&
        lengthOfMessage(hashAlgorithm) == hashedMessage.length
}

/**
 * Time Stamp Request as seen in RFC 3161 section 2.4.1.
 *
 * @property version MUST be 1 at the time of this writing.
 *
 * @property messageImprint a hash algorithm OID and the hash value of
 * the data to be time-stamped.
 *
 * @property reqPolicy indicates the TSA policy under which the
 * TimeStampToken SHOULD be provided.
 *
 * @property nonce allows the client to verify the timeliness of
 * the response when no local clock is available.  The nonce is a large
 * random number with a high probability that the client generates it
 * only once (e.g., a 64 bit integer).  In such a case the same nonce
 * value MUST be included in the response, otherwise the response shall
 * be rejected.
 */
data class Request(
    val version: Int,
    val messageImprint: MessageImprint,
    val reqPolicy: String?,
    val nonce: BigInteger?
) {
    private fun validVersion(): Boolean = version == 1
    fun valid(): Boolean = validVersion() && messageImprint.valid()
}
