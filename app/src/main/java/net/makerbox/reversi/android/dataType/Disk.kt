package net.makerbox.reversi.android.dataType

enum class Disk {
    DARK,
    LIGHT;

    fun flipped(): Disk {
        return when (this) {
            DARK -> LIGHT
            LIGHT -> DARK
        }
    }
}

// TODO: flip()を実装したかったけどできなかった