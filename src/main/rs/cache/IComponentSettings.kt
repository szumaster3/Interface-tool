package rs.cache

class IComponentSettings(var settings: Int, var mask: Int) {
    fun rightClickAllowed(): Boolean = (settings and 0x1) != 0
    fun isRightClickOptionAllowed(optionID: Int): Boolean {
        require(optionID in 1..10) { "optionID must be 1-10" }
        return ((settings shr optionID) and 0x1) != 0
    }
    fun hasUnlockedSlots(): Boolean = ((settings and 0x2EAA42) shr 13) != 0
    val useOnMask: Int get() = (settings shr 11) and 0x7F
    fun canUseOnGroundItems(): Boolean = (useOnMask and 0x1) != 0
    fun canUseOnNpcs(): Boolean = (useOnMask and 0x2) != 0
    fun canUseOnObjects(): Boolean = (useOnMask and 0x4) != 0
    fun canUseOnOtherPlayers(): Boolean = (useOnMask and 0x8) != 0
    fun canUseOnSelf(): Boolean = (useOnMask and 0x10) != 0
    fun canUseOnInterfaceComponent(): Boolean = (useOnMask and 0x20) != 0
    fun interfaceEventDepth(): Int = (settings shr 18) and 0x7
    fun group1Flags(): Boolean = ((settings and 0x55F65FB5) shr 18) != 0
    fun group2Flags(): Boolean = ((settings and 0x1FA3C81F) shr 20) != 0
    fun unknownBit21(): Boolean = ((settings shr 21) and 0x1) != 0
    fun canBeUsedOn(): Boolean = ((settings shr 22) and 0x1) != 0
    fun unknownBits28to30(): Boolean = ((settings shr 28) and 0x7) != 0
    fun unknownBit31(): Boolean = ((settings shr 31) and 0x1) != 0
}