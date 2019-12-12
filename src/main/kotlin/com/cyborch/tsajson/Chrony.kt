package com.cyborch.tsajson

import kotlin.streams.toList

/**
 * Latest time offset from NTP server.
 *
 * Retrieve the last offset from chrony client. A prerequisite is that the
 * chrony client must be installed on this machine. The chrony client has a csv
 * output which is expected to emit the last offset as the 6th column of the
 * first row.
 *
 * The chrony documentation can be found here:
 * https://chrony.tuxfamily.org/doc/3.5/chronyc.html#tracking
 *
 * @return Last offset in seconds.
 */
fun chronyOffset() =
    Runtime
        .getRuntime()
        .exec("/usr/bin/chronyc -c tracking")
        .inputStream
        .bufferedReader(charset("UTF-8"))
        .lines()
        .toList()
        .first()
        .split(",")
        .get(5)
        .toDouble()
