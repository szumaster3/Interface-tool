package rs2.cache.loaders


class IComponentSettings(var settings: Int, var mask: Int) {
    fun hasUnlockedSlots(): Boolean {
        return ((settings and 0x2EAA42) shr 13) != 0
    }

    fun unlockedSlot(slot: Int): Boolean {
        return ((settings shr (1 + slot)) and 0x1) != 0
    }

    fun someGroup1(): Boolean {
        return ((settings and 0x55F65FB5) shr 18) != 0
    }

    fun someGroup2(): Boolean {
        return ((settings and 0x1FA3C81F) shr 20) != 0
    }

    fun bit0(): Boolean {
        return (settings and 0x1) != 0
    }

    fun bit1(): Boolean {
        return ((settings shr 22) and 0x1) != 0
    }

    fun mask3Bits(): Int {
        return (settings shr 18) and 0x7
    }

    val targetCursorMask: Int
        get() = (settings and 0x3FDA8) shr 11


    fun get7BitMask(): Int {
        return (settings shr 7) and 0x7F
    }
}
