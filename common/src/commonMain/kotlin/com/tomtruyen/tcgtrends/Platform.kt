package com.tomtruyen.tcgtrends

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform