package com.cyborch.tsajson

import java.io.File
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Paths

private var serial = BigInteger.ZERO

/**
 * Initialize the serial number with the contents of the stored serial number.
 */
fun initialize() {
    try {
        val content = String(Files.readAllBytes(Paths.get("/var/lib/tsa/serial.num")))
        serial = BigInteger(content, 10)
    } catch (ex: Exception) {
        serial = BigInteger.ZERO
    }
}

/**
 * Update the serial number internally and on disk and return the next serial number.
 *
 * @return a BigInteger with the next serial number.
 */
fun nextSerial(): BigInteger {
    serial = serial.add(BigInteger.ONE)
    serial.toString(10)
    File("/var/lib/tsa/serial.num").writeText(serial.toString(10))
    return serial
}
