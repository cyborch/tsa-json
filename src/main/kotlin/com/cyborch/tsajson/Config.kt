package com.cyborch.tsajson

import java.io.FileInputStream
import java.util.*

private var props: Properties? = null

/**
 * Server configuration.
 */
fun config(): Properties {
    if (props != null) return props!!
    val pwd = System.getProperty("user.dir")
    val filename = "${pwd}/config/server.properties"
    props = Properties()
    props?.load(
            FileInputStream(filename)
                .reader(
                    charset("UTF-8")
                )
        )
    return props!!
}
