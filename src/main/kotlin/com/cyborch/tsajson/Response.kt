package com.cyborch.tsajson

import com.google.gson.GsonBuilder
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.*

/*
* // Each section is separated by a dot
// JWS header, must be base64 encoded
{
  "typ": "Time-Stamp",
  "alg": "SHA1withRSA"
}
// Response, must be base64 encoded
{
  "status": {
    "status": 0, // granted
    "statusString": null,
    "failInfo": null
  },
  "timeStampToken": {
    "contentType": "id-signedData",
    "content": {
      "version": 1,
      "policy": "policy1", // from request
      "messageImprint": "40f1f310cbae011642d33edbad742ea3c581864d", // from request
      "serialNumber": "4324222", // The serialNumber field is an integer assigned by the TSA to each TimeStampToken.  It MUST be unique for each TimeStampToken issued by a given TSA
      "genTime": "19990609001326.34352Z", // YYYYMMDDhhmmss[.s...]Z
      "accuracy": {
        "seconds": 0,
        "millis": 0,
        "micros": 42
      },
      "ordering": false,
      "nonce": "3455345232345454" // from request
    }
  }
}
// base64 encoded signature follows

* */
private val gson = GsonBuilder()
    .setDateFormat("yyyyMMddHHmmss'Z'")
    .create()

data class Header(
    val typ: String = "Time-Stamp",
    val alg: String = "SHA1withRSA"
)

data class Status(
    val status: Int,
    val statusString: String? = null,
    val failInfo: String? = null
)

data class Accuracy(
    val seconds: Int,
    val millis: Int = 0,
    val micros: Int = 0
)

data class Content(
    val version: Int,
    val policy: String?,
    val messageImprint: String,
    val serialNumber: BigInteger,
    val genTime: Date,
    val accuracy: Accuracy,
    val ordering: Boolean,
    val nonce: BigInteger?
)

data class TimeStampToken(
    val contentType: String,
    val content: Content
)

data class Response(
    val status: Status,
    val timeStampToken: TimeStampToken?
)

private fun encode(text: ByteArray) = Base64
    .getEncoder()
    .encode(text)
    .toString(charset("UTF-8"))

private fun encode(text: String) = encode(text
    .toByteArray(charset("UTF-8")))

fun response(request: Request): String {
    val header = encode(gson.toJson(Header()))
    var offset = chronyOffset()
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
                request.reqPolicy,
                request.messageImprint.hashedMessage,
                nextSerial(),
                Date(),
                Accuracy(seconds, millis, micros),
                false,
                request.nonce
            )
        )
    )
    val payload = gson.toJson(response)
    val signature = encode(sign(payload, loadPrivateKey()))
    return header + "." + encode(payload) + "." + signature
}
