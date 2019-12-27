package com.cyborch.tsajson

import com.google.gson.GsonBuilder
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwx.HeaderParameterNames
import java.lang.Exception
import java.math.BigInteger
import java.util.*


private val gson = GsonBuilder()
    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    .create()

/**
 * Status of a response.
 *
 * @property status 0 on granted, 1 on rejected
 *
 * @property statusString
 * One of
 *
 *  - badAlg: unrecognized or unsupported Algorithm Identifier
 *  - badRequest: transaction not permitted or supported
 *  - badDataFormat: the data submitted has the wrong format
 *  - timeNotAvailable: the TSA's time source is not available
 *  - systemFailure: the request cannot be handled due to system failure
 */
data class Status(
    val status: Int,
    val statusString: String? = null
)

/**
 * Content of a TimeStampToken in a Response.
 *
 * @see TimeStampToken
 *
 * @see Response
 *
 * @see Request
 *
 * @property version 2 at the time of this writing
 *
 * @property message MUST have the same value as the similar field in Request
 *
 * @property genTime the time at which the time-stamp token has been created by
 * the TSA.
 *
 * @property radius
 * Radius (in microseconds) is used to indicate the server’s certainty
 * about the reported time. For example, a radius of 1,000,000μs means the
 * server is reasonably sure that the true time is within one second of the
 * reported time
 *
 * @property nonce
 * The nonce field MUST be present if it was present in the
 * Request. In such a case it MUST equal the value provided in the
 * Request structure.
 */
data class TimeStampToken(
    val version: Int,
    val message: Any,
    val genTime: Date,
    val radius: BigInteger,
    val nonce: BigInteger?
)

/**
 * The status of the time stamp response.
 *
 * @see Status
 *
 * @see TimeStampToken
 */
data class Response(
    val status: Status,
    val timeStampToken: TimeStampToken?
)

/**
 * Create a response to a request.
 *
 * @see Request
 *
 * @see Response
 *
 * @return Base64url encoded, dot (.) delimited header, response, and signature
 */
fun response(request: Request?): String {
    val jws = JsonWebSignature()
    jws.algorithmHeaderValue = AlgorithmIdentifiers.RSA_USING_SHA256
    jws.setHeader(HeaderParameterNames.X509_URL, config().getProperty(HeaderParameterNames.X509_URL))
    jws.setHeader(HeaderParameterNames.TYPE, "application/timestamped-json")
    jws.setHeader(HeaderParameterNames.JWK_SET_URL, config().getProperty(HeaderParameterNames.JWK_SET_URL))
    jws.key = loadPrivateKey()
    try {
        val time = roughTime()
        if (request != null) {
            val response = Response(
                Status(0),
                TimeStampToken(
                    2,
                    request.message,
                    time.midpoint,
                    time.radius,
                    request.nonce
                )
            )
            jws.payload = gson.toJson(response)
            return jws.compactSerialization
        } else {
            val error = "badDataFormat"
            val response = Response(
                Status(1, error),
                null
            )
            jws.payload = gson.toJson(response)
            return jws.compactSerialization
        }
    } catch (ex: Exception) {

        print(ex)
        val error = "timeNotAvailable"
        val response = Response(
            Status(1, error),
            null
        )
        jws.payload = gson.toJson(response)
        return jws.compactSerialization
    }

}
