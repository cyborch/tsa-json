package com.cyborch.tsajson

import io.javalin.http.Context
import java.lang.Exception
import java.math.BigInteger

/**
 * Time Stamp Request
 *
 * @property version MUST be 2 at the time of this writing.
 *
 * @property message The message to be time-stamped, which can be any
 * json value.
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
    val message: Any,
    val nonce: BigInteger?
) {
    private fun validVersion(): Boolean = version == 2
    fun valid(): Boolean = validVersion()

    companion object {
        fun from(context: Context): Request? {
            try {
                val request = context.bodyAsClass(Request::class.java)
                if (!request.valid()) { return null }
                return request
            } catch (ex: Exception) {
                return null
            }
        }
    }
}
