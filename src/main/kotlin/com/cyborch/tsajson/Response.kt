package com.cyborch.tsajson

import com.google.gson.GsonBuilder
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwx.HeaderParameterNames
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.*


private val gson = GsonBuilder()
    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    .create()

/**
 * A JWS header with a "Time-Stamp" type and a "RS256" algorithm.
 *
 * @property x5u the URL of the certificate containing the public key
 * used to verify the signature.
 */
data class Header(
    val typ: String = "Time-Stamp",
    val alg: String = "RS256",
    val x5u: String
)

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
 * The time deviation around the UTC time contained in genTime in Content.
 *
 * If either seconds, millis or micros is missing, then a value of zero
 * MUST be taken for the missing field.
 *
 * By adding the accuracy value to the genTime, an upper limit
 * of the time at which the time-stamp token has been created by the TSA
 * can be obtained.  In the same way, by subtracting the accuracy to the
 * genTime, a lower limit of the time at which the time-stamp
 * token has been created by the TSA can be obtained.
 *
 * @see Content
 *
 * @property seconds an integer between -999 and 999
 *
 * @property millis an integer between -999 and 999
 *
 * @property micros an integer between -999 and 999
 */
data class Accuracy(
    val seconds: Int,
    val millis: Int = 0,
    val micros: Int = 0
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
 * @property version 1 at the time of this writing
 *
 * @property context MUST have the same value as the similar field in Request
 *
 * @property messageImprint MUST have the same value as the similar field in Request
 *
 * @property serialNumber MUST be unique for each TimeStampToken issued by
 * a given TSA (i.e., the TSA name and serial number identify a unique
 * TimeStampToken)
 *
 * @property genTime the time at which the time-stamp token has been created by
 * the TSA.
 *
 * @property accuracy the time deviation around the UTC time contained in genTime
 *
 * @property ordering if set to true, every time-stamp
 * token from the same TSA can always be ordered based on the genTime
 * field, regardless of the genTime accuracy
 *
 * @property nonce
 * The nonce field MUST be present if it was present in the
 * Request. In such a case it MUST equal the value provided in the
 * Request structure.
 */
data class Content(
    val version: Int,
    val context: String?,
    val messageImprint: String,
    val serialNumber: BigInteger,
    val genTime: Date,
    val accuracy: Accuracy,
    val ordering: Boolean,
    val nonce: BigInteger?
)

/**
 * Content type and content of the response.
 *
 * In version 1, content will always be set to "id-signedData"
 *
 * @see Content
 *
 * @property contentType set to "id-signedData"
 *
 * @property content the content of the time stamp token
 */
data class TimeStampToken(
    val contentType: String,
    val content: Content
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
    jws.setHeader(HeaderParameterNames.TYPE, "application/timestamp-reply+jws")
    jws.setHeader(HeaderParameterNames.JWK_SET_URL, config().getProperty(HeaderParameterNames.JWK_SET_URL))
    jws.key = loadPrivateKey()
    var offset = chronyOffset()
    if (request != null && offset != null) {
        val seconds = BigDecimal(offset).setScale(0, RoundingMode.DOWN).toInt()
        offset -= seconds
        val millis = BigDecimal(offset * 1000).setScale(0, RoundingMode.DOWN).toInt()
        offset -= millis
        val micros = BigDecimal(offset * 1000).setScale(0, RoundingMode.DOWN).toInt()
        val response = Response(
            Status(0),
            TimeStampToken(
                "id-signedData",
                Content(
                    1,
                    request.context,
                    request.messageImprint.hashedMessage,
                    nextSerial(),
                    Date(),
                    Accuracy(seconds, millis, micros),
                    false,
                    request.nonce
                )
            )
        )
        jws.payload = gson.toJson(response)
        return jws.compactSerialization
    } else {
        var error = "systemFailure";
        if (request == null) { error = "badDataFormat" }
        else if (offset == null) { error = "timeNotAvailable" }
        val response = Response(
            Status(1,error),
            null
        )
        jws.payload = gson.toJson(response)
        return jws.compactSerialization
    }
}
