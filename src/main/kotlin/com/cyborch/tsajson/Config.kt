package com.cyborch.tsajson

import java.io.FileInputStream
import java.util.*

/**
 * Server configuration.
 */
fun config(): Properties {
    val filename = "/opt/tsa/config/server.properties"
    val props = Properties()
    props.load(
            FileInputStream(filename)
                .reader(
                    charset("UTF-8")
                )
        )
    return props
}
