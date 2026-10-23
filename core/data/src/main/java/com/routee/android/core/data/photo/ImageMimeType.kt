package com.routee.android.core.data.photo

import java.io.InputStream
import java.net.URLConnection
import java.util.Locale

internal fun imageMimeType(input: InputStream, provided: String?, extension: String?): String =
    sequenceOf(URLConnection.guessContentTypeFromStream(input), provided, extension)
        .mapNotNull { it?.substringBefore(';')?.trim()?.lowercase(Locale.ROOT) }
        .firstOrNull { it.startsWith("image/") }
        ?: error("Unknown image format")
