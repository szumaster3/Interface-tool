package rs2.cache.loaders.legacy

data class ComponentData(
    var componentId: Int = 0,
    var interfaceId: Int = 0,
    var type: Int = 0,
    var buttonType: Int = 0,
    var contentType: Int = 0,
    var baseX: Int = 0,
    var baseY: Int = 0,
    var baseWidth: Int = 0,
    var baseHeight: Int = 0,
    var transparency: Int = 0,
    var parentId: Int = -1,
    var unknownProp_11: Int = 0,
    var objCounts: IntArray? = null,
    var objTypes: IntArray? = null,
    var invOffsetX: IntArray? = null,
    var invOffsetY: IntArray? = null,
    var invSprite: IntArray? = null,
    var invOptions: Array<String?>? = null,
    var cs1ComparisonOpcodes: IntArray? = null,
    var cs1ComparisonOperands: IntArray? = null,
    var cs1Scripts: Array<IntArray>? = null
) {
    private fun safeGet(arr: IntArray?, index: Int) = if (arr != null && index in arr.indices) arr[index] else 0

    private fun safeGetOption(arr: Array<String?>?, index: Int) = if (arr != null && index in arr.indices) arr[index] else null

    /**
     * Parses the client-side CS1 scripts for a given component.
     *
     * Each component may have multiple script blocks (opcodes), which are evaluated to produce a numeric result used for interface logic
     * (e.g., enabling/disabling, showing values, etc.).
     *
     * @param opcodeComponents Map of interface IDs to arrays of components. Each array index corresponds to a component ID.
     * @param currentStats Array of the player's current skill levels.
     * @param maxStats Array of the player's maximum skill levels.
     * @param currentExp Array of the player's current experience points per skill.
     * @param xpForLevel Array of XP required for each level.
     * @param variousSettings Array of client variables (varps / settings) used in script evaluation.
     * @param skillEnabled Boolean array indicating which skills are currently enabled.
     * @param energy The player's current energy level.
     * @param weight The player's current carried weight.
     * @param playerX The player's current X coordinate in the game world.
     * @param playerY The player's current Y coordinate in the game world.
     * @param baseXMap The base X coordinate of the loaded map region.
     * @param baseYMap The base Y coordinate of the loaded map region.
     * @return The numeric result of evaluating the script, or a special value if an error occurs (e.g., -1 for exception).
     * @author Leanbow
     * @since 30 Dec 2011
     */
    fun parseCS1(
        idx: Int,
        opcodeComponents: Map<Int, Array<ComponentData?>>,
        currentStats: IntArray,
        maxStats: IntArray,
        currentExp: IntArray,
        xpForLevel: IntArray,
        variousSettings: IntArray,
        skillEnabled: BooleanArray,
        energy: Int,
        weight: Int,
        playerX: Int,
        playerY: Int,
        baseXMap: Int,
        baseYMap: Int
    ): Int {
        if (cs1Scripts == null || idx >= cs1Scripts!!.size) return -2
        val opcodes = cs1Scripts!![idx]

        var result = 0
        var counter = 0
        var type = 0

        try {
            while (counter < opcodes.size) {
                val opcode = opcodes[counter++]
                var value = 0
                var tempType = 0

                when (opcode) {
                    0 -> return result
                    1 -> value = safeGet(currentStats, opcodes.getOrNull(counter++) ?: 0)
                    2 -> value = safeGet(maxStats, opcodes.getOrNull(counter++) ?: 0)
                    3 -> value = safeGet(currentExp, opcodes.getOrNull(counter++) ?: 0)
                    4 -> { // inventory count opcode
                        val inter = safeGetComponent(opcodeComponents, opcodes, counter) ?: null
                        counter += 3 // skip interId, unused, itemId
                        if (inter != null) {
                            val itemId = opcodes.getOrNull(counter - 1) ?: -1
                            if (itemId != -1) {
                                for (i in inter.objCounts?.indices ?: 0..-1) {
                                    if ((safeGet(inter.objTypes, i) == itemId)) {
                                        value += safeGet(inter.objCounts, i)
                                    }
                                }
                            }
                        }
                    }
                    5 -> value = safeGet(variousSettings, opcodes.getOrNull(counter++) ?: 0)
                    6 -> value = xpForLevel.getOrElse(safeGet(maxStats, opcodes.getOrNull(counter++) ?: 1) - 1) { 0 }
                    7 -> value = (safeGet(variousSettings, opcodes.getOrNull(counter++) ?: 0) * 100) / 46875
                    8 -> value = 0
                    9 -> {
                        for (i in 0 until skillEnabled.size) {
                            if (skillEnabled.getOrNull(i) == true) value += maxStats.getOrElse(i) { 0 }
                        }
                    }
                    10 -> { // inventory contains object
                        val inter = safeGetComponent(opcodeComponents, opcodes, counter) ?: null
                        counter += 3
                        val itemId = opcodes.getOrNull(counter - 1) ?: -1
                        if (inter != null && itemId != -1) {
                            for (i in inter.objTypes?.indices ?: 0..-1) {
                                if (safeGet(inter.objTypes, i) == itemId) {
                                    value = 999_999_999
                                    break
                                }
                            }
                        }
                    }
                    11 -> value = energy
                    12 -> value = weight
                    13 -> { // varp bit test
                        val i1 = safeGet(variousSettings, opcodes.getOrNull(counter++) ?: 0)
                        val i2 = opcodes.getOrNull(counter++) ?: 0
                        value = if ((1 shl i2 and i1) == 0) 0 else 1
                    }
                    14 -> { // varbit
                        val varbitId = opcodes.getOrNull(counter++) ?: 0
                        value = variousSettings.getOrElse(varbitId) { 0 }
                    }
                    15 -> tempType = 1
                    16 -> tempType = 2
                    17 -> tempType = 3
                    18 -> value = baseXMap + (playerX shr 7)
                    19 -> value = baseYMap + (playerY shr 7)
                    20 -> value = opcodes.getOrNull(counter++) ?: 0
                }

                when (tempType) {
                    0 -> {
                        when (type) {
                            0 -> result += value
                            1 -> result -= value
                            2 -> if (value != 0) result /= value
                            3 -> result *= value
                        }
                        type = 0
                    }
                    else -> type = tempType
                }
            }
        } catch (e: Exception) {
            return -1
        }
        return result
    }

    private fun safeGetComponent(opcodeComponents: Map<Int, Array<ComponentData?>>, opcodes: IntArray, counter: Int): ComponentData? {
        val interId = ((opcodes.getOrNull(counter) ?: 0) shl 16) + (opcodes.getOrNull(counter + 1) ?: 0)
        val compId = interId and 0xFFFF
        val ifaceId = interId shr 16
        return opcodeComponents[ifaceId]?.getOrNull(compId)
    }

    /**
     * Checks whether the component is active.
     */
    fun isEnabled(
        opcodeComponents: Map<Int, Array<ComponentData?>>,
        currentStats: IntArray,
        maxStats: IntArray,
        currentExp: IntArray,
        xpForLevel: IntArray,
        variousSettings: IntArray,
        skillEnabled: BooleanArray,
        energy: Int,
        weight: Int,
        playerX: Int,
        playerY: Int,
        baseXMap: Int,
        baseYMap: Int
    ): Boolean {
        if (cs1ComparisonOpcodes == null || cs1ComparisonOperands == null) return false
        for (i in cs1ComparisonOpcodes!!.indices) {
            val value =
                parseCS1(
                    i,
                    opcodeComponents,
                    currentStats,
                    maxStats,
                    currentExp,
                    xpForLevel,
                    variousSettings,
                    skillEnabled,
                    energy,
                    weight,
                    playerX,
                    playerY,
                    baseXMap,
                    baseYMap
                )
            val req = cs1ComparisonOperands!!.getOrNull(i) ?: 0
            when (cs1ComparisonOpcodes!![i]) {
                2 -> if (value >= req) return false
                3 -> if (req >= value) return false
                4 -> if (value == req) return false
                else -> if (value != req) return false
            }
        }
        return true
    }
}
