package com.cyborch.tsajson

import com.google.gson.GsonBuilder
import java.math.BigInteger
import java.util.*
import kotlin.streams.toList

private val gson = GsonBuilder()
    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    .create()

data class RoughTime(
    val midpoint: Date,
    val radius: BigInteger
)

/**
 * Latest time from RoughTime server.
 *
 * Retrieve the last offset from roughenough client. A prerequisite is that the
 * roughenough client must be installed on this machine. The roughenough client
 * has a json output which is expected to contain midpoint and radius keys.
 *
 * @return RoughTime
 */
fun roughTime() =
    gson.fromJson<RoughTime>(
        Runtime
            .getRuntime()
            .exec("${config().getProperty("client")} " +
                    "--json " +
                    "--time-format %Y-%m-%dT%H:%M:%S%z " +
                    "${config().getProperty("roughtime")} " +
                    "2002")
            .inputStream
            .bufferedReader(charset("UTF-8"))
            .lines()
            .toList()
            .first(),
        RoughTime::class.java
    )
