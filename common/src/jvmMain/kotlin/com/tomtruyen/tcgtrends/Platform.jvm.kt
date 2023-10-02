package com.tomtruyen.tcgtrends

class JvmPlatform : Platform {
    override val name: String = "API"
}

actual fun getPlatform(): Platform = JvmPlatform()