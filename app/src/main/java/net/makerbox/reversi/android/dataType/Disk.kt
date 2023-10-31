package net.makerbox.reversi.android.dataType

enum class Disk() {
    DARK,
    LIGHT;

    fun flipped(): Disk {
        return when (this) {
            DARK -> LIGHT
            LIGHT -> DARK
        }
    }

    companion object {
        fun sides(): List<Disk> {
            return listOf(DARK, LIGHT)
        }
    }

    val index
        get(): Int {
        return when (this) {
            DARK -> 0
            LIGHT -> 1
        }
    }
}



// TODO: flip()を実装したかったけどできなかった