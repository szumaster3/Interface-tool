package com.alex.loaders.interfaces

import com.alex.Cache
import com.alex.io.InputStream
import com.alex.io.OutputStream
import com.alex.utils.Utils
import java.io.ByteArrayInputStream

class ComponentDefinition {
    internal var version: Byte = 0
    var type: Int = 0
    var contentType: Int = 0
    var componentHash: Int = 0
    var interfaceId: Int = 0
    var componentId: Int = 0

    var positionX: Int = 0
    var positionY: Int = 0
    var baseX: Int = 0
    var baseY: Int = 0
    var baseWidth: Int = 0
    var baseHeight: Int = 0

    var width: Int = 0
    var height: Int = 0

    var modelObjWidth: Int = 0
    var modelObjHeight: Int = 0
    var textLineHeight: Int = 0
    var scrollHeight: Int = 0
    var scrollWidth: Int = 0
    var mouseOverCursor: Int
    var targetOverCursor: Int = 0
    var targetLeaveCursor: Int = 0
    var parentId: Int = 0
    var xMode: Byte = 0
    var yMode: Byte = 0

    var spriteId: Int = 0
    var hidden: Boolean = false
    var filled: Boolean = false
    var shadow: Boolean = false
    var shadowColor: Int = 0
    var hasAlpha: Boolean = false
    var noClickThrough: Boolean = false
    var dragRenderBehavior: Boolean = false
    var spriteTiling: Boolean = false

    var vFlip: Boolean = false
    var hFlip: Boolean = false

    var text: String? = ""
    var textVerticalAli: Int = 0
    var textHorizontalAli: Int = 0
    var name: String? = null
    var fontId: Int = 0

    var modelId: Int = 0
    var modelOriginX: Int = 0
    var modelOriginY: Int = 0
    var modelXAngle: Int = 0
    var modelYAngle: Int = 0
    var modelZAngle: Int = 0
    var modelZoom: Int = 0
    var modelAnimId: Int = 0
    var modelOrtho: Boolean = false
    var unknownModelProp3: Int = 0
    var unknownModelProp4: Int = 0
    var unknownModelProp5: Boolean = false

    var color: Int = 0
    var transparency: Int = 0
    var borderThickness: Int = 0
    var angle2d: Int = 0
    var widthMode: Byte = 0
    var heightMode: Byte = 0
    var modelType: Int = 1

    var rightClickOptions: Array<String?>? = null
    var optionBase: String? = ""
    var optionCircumfix: String? = ""
    var optionMask: Int = 0
    var Option: Int = 0
    var mask = -1

    var dragDeadtime: Int = 0
    var dragDeadzone: Int = 0

    lateinit var settings: IComponentSettings
    var hasScripts: Boolean = false
    var onLoadScript: Array<Any?>? = null
    var onMouseHoverScript: Array<Any?>? = null
    var onMouseLeaveScript: Array<Any?>? = null
    var onUseWith: Array<Any?>? = null
    var onUse: Array<Any?>? = null
    var onVarpTransmit: Array<Any?>? = null
    var onInvTransmit: Array<Any?>? = null
    var onStatTransmit: Array<Any?>? = null
    var onTimer: Array<Any?>? = null
    var onOptionClick: Array<Any?>? = null
    var onMouseRepeat: Array<Any?>? = null
    var onClickRepeat: Array<Any?>? = null
    var onDrag: Array<Any?>? = null
    var onRelease: Array<Any?>? = null
    var onHold: Array<Any?>? = null
    var onDragStart: Array<Any?>? = null
    var onDragRelease: Array<Any?>? = null
    var onScroll: Array<Any?>? = null
    var onVarcTransmit: Array<Any?>? = null
    var onVarcStrTransmit: Array<Any?>? = null

    var varpTriggers: IntArray? = null
    var inventoryTriggers: IntArray? = null
    var statTriggers: IntArray? = null
    var varcTriggers: IntArray? = null
    var varcstrTriggers: IntArray? = null

    var opCursors: IntArray? = null

    var lineWidth: Int = 0
    var unknownProp_8: Boolean = false

    var opId: IntArray? = null
    var repeatCount: ByteArray? = null
    var modifier: ByteArray? = null

    private var objCounts: IntArray? = null
    private var objTypes: IntArray? = null

    // ---- Fields of the old IF1 format (they do not exist in IF3) ----

    /** True for components stored in the old IF1 format. */
    val isIf1: Boolean get() = version.toInt() == 1

    /** 0 none, 1 ok, 2 target, 3 close, 4 toggle, 5 select, 6 continue */
    var buttonType = 0

    /** Conditions (opcode, operand) which decide when the CS1 scripts below run. */
    var cs1ComparisonOpcodes: IntArray? = null
    var cs1ComparisonOperands: IntArray? = null

    /** CS1 scripts, every script is a list of numbers. */
    var cs1Scripts: Array<IntArray>? = null

    /** Text and colours of the active (selected) and the hovered state. */
    var activeText: String? = null
    var activeColor = 0
    var overColor = 0
    var unknownColor = 0

    var unknownProp_11 = 0

    /** Option of the button types 1, 4, 5 and 6, the suffix and the target mask of the button type 2. */
    var option: String = ""
    var optionSuffix: String = ""
    var if1Mask = 0

    var activeSpriteId = -1
    var activeModelType = 0
    var activeModelId = -1
    var activeModelAnimId = -1

    /** Inventory (types 2 and 7): the margin between the slots, the options and the 4 flag bytes. */
    var invMarginX = 0
    var invMarginY = 0
    var invOptions: Array<String?>? = null
    var invFlagBytes = IntArray(4)

    /** Inventory (type 2): 20 slots with a placeholder sprite which is shown when the slot is empty. */
    var invSlotPresent = BooleanArray(20)
    var invOffsetX: IntArray? = null
    var invOffsetY: IntArray? = null
    var invSprite: IntArray? = null

    /** Values of the types 1 and 7 which the client reads but does not use. */
    var if1Type1A = 0
    var if1Type1B = 0
    var if1Type7HasOptions = 0

    fun encode(): ByteArray = if (version.toInt() == 1) encodeIf1() else encodeIf3()

    fun encodeIf3(): ByteArray {
        val out = OutputStream()
        out.writeByte(255)

        var t = type and 0x7F
        if (!name.isNullOrEmpty()) {
            out.writeByte(t or 0x80)
            out.writeString(name!!)
        } else {
            out.writeByte(t)
        }

        out.writeShort(contentType)
        out.writeShort(baseX)
        out.writeShort(baseY)
        out.writeShort(baseWidth)
        out.writeShort(baseHeight)
        out.writeByte(widthMode.toInt())
        out.writeByte(heightMode.toInt())
        out.writeByte(yMode.toInt())
        out.writeByte(xMode.toInt())
        out.writeShort(if (parentId != -1) parentId and 0xFFFF else 65535)

        out.writeByte(if (hidden) 1 else 0)

        when (type) {
            ComponentType.LAYER -> {
                out.writeShort(scrollWidth)
                out.writeShort(scrollHeight)
                out.writeByte(if (noClickThrough) 1 else 0)
            }
            ComponentType.SPRITE -> {
                out.writeInt(spriteId)
                out.writeShort(angle2d)
                var spriteFlags = 0
                if (spriteTiling) spriteFlags = spriteFlags or 0x1
                if (hasAlpha) spriteFlags = spriteFlags or 0x2
                out.writeByte(spriteFlags)
                out.writeByte(transparency)
                out.writeByte(borderThickness)
                out.writeInt(shadowColor)
                out.writeByte(if (vFlip) 1 else 0)
                out.writeByte(if (hFlip) 1 else 0)
            }
            ComponentType.MODEL -> {
                out.writeShort(if (modelId == -1) 65535 else modelId)
                out.writeShort(modelOriginX)
                out.writeShort(modelOriginY)
                out.writeShort(modelXAngle)
                out.writeShort(modelYAngle)
                out.writeShort(modelZAngle)
                out.writeShort(modelZoom)
                out.writeShort(if (modelAnimId == -1) 65535 else modelAnimId)
                out.writeByte(if (modelOrtho) 1 else 0)
                out.writeShort(unknownModelProp3)
                out.writeShort(unknownModelProp4)
                out.writeByte(if (unknownModelProp5) 1 else 0)
                if (widthMode.toInt() != 0) out.writeShort(modelObjWidth)
                if (heightMode.toInt() != 0) out.writeShort(modelObjHeight)
            }
            ComponentType.TEXT -> {
                out.writeShort(if (fontId == -1) 65535 else fontId)
                out.writeString(text ?: "")
                out.writeByte(textLineHeight)
                out.writeByte(textHorizontalAli)
                out.writeByte(textVerticalAli)
                out.writeByte(if (shadow) 1 else 0)
                out.writeInt(color)
            }
            ComponentType.FIGURE -> {
                out.writeInt(color)
                out.writeByte(if (filled) 1 else 0)
                out.writeByte(transparency)
            }
            ComponentType.LINE -> {
                out.writeByte(lineWidth.toInt())
                out.writeInt(color)
                out.writeByte(if (unknownProp_8) 1 else 0)
            }
        }

        out.write24BitInt(optionMask)

        if (opId != null) {
            for (i in opId!!.indices) {
                // slots that were never set (all defaults) are not part of the file
                if (opId!![i] == 0 && repeatCount!![i].toInt() == 0 && modifier!![i].toInt() == 0) continue
                val value = if (opId!![i] == -1) 4095 else opId!![i]
                out.writeByte(((i + 1) shl 4) or (value shr 8))
                out.writeByte(value and 0xFF)
                out.writeByte(repeatCount!![i].toInt())
                out.writeByte(modifier!![i].toInt())
            }
            out.writeByte(0)
        } else {
            out.writeByte(0)
        }

        out.writeString(optionBase ?: "")
        val optionCount = rightClickOptions?.size ?: 0
        val activeCursors = mutableListOf<Pair<Int, Int>>()

        if (opCursors != null)
        {
            for (i in opCursors!!.indices)
            {
                val value = opCursors!![i]
                if (value != -1)
                {
                    activeCursors.add(Pair(i, value))
                }
            }
        }

        val cursorFlag = when (activeCursors.size){
            0 -> 0
            1 -> 1
            else -> 2
        }

        out.writeByte((cursorFlag shl 4) or optionCount)

        rightClickOptions?.forEach {
            out.writeString(it ?: "")
        }

        if (cursorFlag > 0)
        {
            val (index1, cursor1) = activeCursors[0]
            out.writeByte(index1)
            out.writeShort(cursor1)

            if (cursorFlag > 1)
            {
                val (index2, cursor2) = activeCursors[1]
                out.writeByte(index2)
                out.writeShort(cursor2)
            }
        }

        out.writeByte(dragDeadzone)
        out.writeByte(dragDeadtime)
        out.writeByte(if (dragRenderBehavior) 1 else 0)
        out.writeString(optionCircumfix ?: "")

        if ((optionMask shr 11 and 0x7F) != 0) {
            out.writeShort(if (mask == -1) 65535 else mask)
            out.writeShort(if (targetOverCursor == -1) 65535 else targetOverCursor)
            out.writeShort(if (targetLeaveCursor == -1) 65535 else targetLeaveCursor)
        }

        encodeScript(onLoadScript, out)
        encodeScript(onMouseHoverScript, out)
        encodeScript(onMouseLeaveScript, out)
        encodeScript(onUseWith, out)
        encodeScript(onUse, out)
        encodeScript(onVarpTransmit, out)
        encodeScript(onInvTransmit, out)
        encodeScript(onStatTransmit, out)
        encodeScript(onTimer, out)
        encodeScript(onOptionClick, out)
        encodeScript(onMouseRepeat, out)
        encodeScript(onClickRepeat, out)
        encodeScript(onDrag, out)
        encodeScript(onRelease, out)
        encodeScript(onHold, out)
        encodeScript(onDragStart, out)
        encodeScript(onDragRelease, out)
        encodeScript(onScroll, out)
        encodeScript(onVarcTransmit, out)
        encodeScript(onVarcStrTransmit, out)

        encodeTriggers(varpTriggers, out)
        encodeTriggers(inventoryTriggers, out)
        encodeTriggers(statTriggers, out)
        encodeTriggers(varcTriggers, out)
        encodeTriggers(varcstrTriggers, out)

        val bytes = ByteArray(out.offset)
        out.offset = 0
        out.getBytes(bytes, 0, bytes.size)
        return bytes
    }

    fun encodeIf1(): ByteArray {
        val out = OutputStream()
        out.writeByte(type)
        out.writeByte(buttonType)
        out.writeShort(contentType)
        out.writeShort(baseX)
        out.writeShort(baseY)
        out.writeShort(baseWidth)
        out.writeShort(baseHeight)
        out.writeByte(transparency)
        out.writeShort(if (parentId != -1) parentId and 0xFFFF else 65535)
        out.writeShort(if (unknownProp_11 == -1) 65535 else unknownProp_11)

        val ops = cs1ComparisonOpcodes
        val operands = cs1ComparisonOperands
        out.writeByte(ops?.size ?: 0)
        if (ops != null && operands != null) {
            for (i in ops.indices) {
                out.writeByte(ops[i])
                out.writeShort(operands[i])
            }
        }
        val scripts = cs1Scripts
        out.writeByte(scripts?.size ?: 0)
        if (scripts != null) {
            for (script in scripts) {
                out.writeShort(script.size)
                for (v in script) out.writeShort(if (v == -1) 65535 else v)
            }
        }

        if (type == ComponentType.LAYER) {
            out.writeShort(scrollHeight)
            out.writeByte(if (hidden) 1 else 0)
        }
        if (type == ComponentType.MODEL_LIST) {
            out.writeShort(if1Type1A)
            out.writeByte(if1Type1B)
        }
        if (type == ComponentType.INVENTORY) {
            for (b in invFlagBytes) out.writeByte(b)
            out.writeByte(invMarginX)
            out.writeByte(invMarginY)
            for (i in 0 until 20) {
                if (invSlotPresent[i]) {
                    out.writeByte(1)
                    out.writeShort(invOffsetX!![i])
                    out.writeShort(invOffsetY!![i])
                    out.writeInt(invSprite!![i])
                } else {
                    out.writeByte(0)
                }
            }
            for (i in 0 until 5) out.writeString(invOptions?.get(i) ?: "")
        }
        if (type == ComponentType.FIGURE) {
            out.writeByte(if (filled) 1 else 0)
        }
        if (type == ComponentType.TEXT || type == ComponentType.MODEL_LIST) {
            out.writeByte(textHorizontalAli)
            out.writeByte(textVerticalAli)
            out.writeByte(textLineHeight)
            out.writeShort(if (fontId == -1) 65535 else fontId)
            out.writeByte(if (shadow) 1 else 0)
        }
        if (type == ComponentType.TEXT) {
            out.writeString(text ?: "")
            out.writeString(activeText ?: "")
        }
        if (type == ComponentType.MODEL_LIST || type == ComponentType.FIGURE || type == ComponentType.TEXT) {
            out.writeInt(color)
        }
        if (type == ComponentType.FIGURE || type == ComponentType.TEXT) {
            out.writeInt(activeColor)
            out.writeInt(overColor)
            out.writeInt(unknownColor)
        }
        if (type == ComponentType.SPRITE) {
            out.writeInt(spriteId)
            out.writeInt(activeSpriteId)
        }
        if (type == ComponentType.MODEL) {
            out.writeShort(if (modelId == -1) 65535 else modelId)
            out.writeShort(if (activeModelId == -1) 65535 else activeModelId)
            out.writeShort(if (modelAnimId == -1) 65535 else modelAnimId)
            out.writeShort(if (activeModelAnimId == -1) 65535 else activeModelAnimId)
            out.writeShort(modelZoom)
            out.writeShort(modelXAngle)
            out.writeShort(modelYAngle)
        }
        if (type == ComponentType.INVENTORY_TEXT) {
            out.writeByte(textHorizontalAli)
            out.writeShort(if (fontId == -1) 65535 else fontId)
            out.writeByte(if (shadow) 1 else 0)
            out.writeInt(color)
            out.writeShort(invMarginX)
            out.writeShort(invMarginY)
            out.writeByte(if1Type7HasOptions)
            for (i in 0 until 5) out.writeString(invOptions?.get(i) ?: "")
        }
        if (type == ComponentType.TOOLTIP) {
            out.writeString(text ?: "")
        }
        if (buttonType == 2 || type == ComponentType.INVENTORY) {
            out.writeString(optionCircumfix ?: "")
            out.writeString(optionSuffix)
            out.writeShort(if1Mask)
        }
        if (buttonType == 1 || buttonType == 4 || buttonType == 5 || buttonType == 6) {
            out.writeString(option)
        }
        val bytes = ByteArray(out.offset)
        out.offset = 0
        out.getBytes(bytes, 0, bytes.size)
        return bytes
    }

    fun decode(
        stream: InputStream,
        componentId: Int,
        interfaceId: Int,
    ) {
        val firstByte = stream.readByte()

        if (firstByte.toInt() == -1) {
            stream.offset -= 1
            decodeIf3(stream, componentId, interfaceId)
        } else {
            stream.offset -= 1
            decodeIf1(stream, componentId, interfaceId)
        }
    }

    private fun decodeIf1(
        stream: InputStream,
        componentId: Int,
        interfaceId: Int,
    ) {
        version = 1
        this.componentId = componentId
        this.interfaceId = interfaceId

        type = stream.readUnsignedByte()
        buttonType = stream.readUnsignedByte()
        contentType = stream.readUnsignedShort()
        baseX = stream.readShort()
        baseY = stream.readShort()
        baseWidth = stream.readUnsignedShort()
        baseHeight = stream.readUnsignedShort()
        widthMode = 0
        heightMode = 0
        yMode = 0
        xMode = 0
        transparency = stream.readUnsignedByte()
        val overlayer = stream.readUnsignedShort()
        parentId = if (overlayer == 65535) -1 else (interfaceId shl 16) + overlayer
        unknownProp_11 = stream.readUnsignedShort().let { if (it == 65535) -1 else it }

        val comparisons = stream.readUnsignedByte()
        if (comparisons > 0) {
            cs1ComparisonOpcodes = IntArray(comparisons)
            cs1ComparisonOperands = IntArray(comparisons)
            for (i in 0 until comparisons) {
                cs1ComparisonOpcodes!![i] = stream.readUnsignedByte()
                cs1ComparisonOperands!![i] = stream.readUnsignedShort()
            }
        }
        val scriptCount = stream.readUnsignedByte()
        if (scriptCount > 0) {
            cs1Scripts = Array(scriptCount) { IntArray(0) }
            for (i in 0 until scriptCount) {
                val length = stream.readUnsignedShort()
                val script = IntArray(length)
                for (j in 0 until length) {
                    script[j] = stream.readUnsignedShort().let { if (it == 65535) -1 else it }
                }
                cs1Scripts!![i] = script
            }
        }

        var flags = 0
        if (type == ComponentType.LAYER) {
            scrollHeight = stream.readUnsignedShort()
            hidden = stream.readUnsignedByte() == 1
        }
        if (type == ComponentType.MODEL_LIST) {
            if1Type1A = stream.readUnsignedShort()
            if1Type1B = stream.readUnsignedByte()
        }
        if (type == ComponentType.INVENTORY) {
            widthMode = 3
            heightMode = 3
            for (i in 0 until 4) invFlagBytes[i] = stream.readUnsignedByte()
            if (invFlagBytes[0] == 1) flags = 0x10000000
            if (invFlagBytes[1] == 1) flags = flags or 0x40000000
            if (invFlagBytes[2] == 1) flags = flags or Int.MIN_VALUE
            if (invFlagBytes[3] == 1) flags = flags or 0x20000000
            invMarginX = stream.readUnsignedByte()
            invMarginY = stream.readUnsignedByte()
            invOffsetX = IntArray(20)
            invOffsetY = IntArray(20)
            invSprite = IntArray(20)
            invSlotPresent = BooleanArray(20)
            for (i in 0 until 20) {
                if (stream.readUnsignedByte() == 1) {
                    invSlotPresent[i] = true
                    invOffsetX!![i] = stream.readShort()
                    invOffsetY!![i] = stream.readShort()
                    invSprite!![i] = stream.readInt()
                } else {
                    invSprite!![i] = -1
                }
            }
            invOptions = arrayOfNulls(5)
            for (i in 0 until 5) {
                val o = Utils.getJagString(stream)
                if (o.isNotEmpty()) {
                    invOptions!![i] = o
                    flags = flags or (1 shl (i + 23))
                }
            }
        }
        if (type == ComponentType.FIGURE) {
            filled = stream.readUnsignedByte() == 1
        }
        if (type == ComponentType.TEXT || type == ComponentType.MODEL_LIST) {
            textHorizontalAli = stream.readUnsignedByte()
            textVerticalAli = stream.readUnsignedByte()
            textLineHeight = stream.readUnsignedByte()
            fontId = stream.readUnsignedShort().let { if (it == 65535) -1 else it }
            shadow = stream.readUnsignedByte() == 1
        }
        if (type == ComponentType.TEXT) {
            text = Utils.getJagString(stream)
            activeText = Utils.getJagString(stream)
        }
        if (type == ComponentType.MODEL_LIST || type == ComponentType.FIGURE || type == ComponentType.TEXT) {
            color = stream.readInt()
        }
        if (type == ComponentType.FIGURE || type == ComponentType.TEXT) {
            activeColor = stream.readInt()
            overColor = stream.readInt()
            unknownColor = stream.readInt()
        }
        if (type == ComponentType.SPRITE) {
            spriteId = stream.readInt()
            activeSpriteId = stream.readInt()
        }
        if (type == ComponentType.MODEL) {
            modelType = 1
            modelId = stream.readUnsignedShort().let { if (it == 65535) -1 else it }
            activeModelType = 1
            activeModelId = stream.readUnsignedShort().let { if (it == 65535) -1 else it }
            modelAnimId = stream.readUnsignedShort().let { if (it == 65535) -1 else it }
            activeModelAnimId = stream.readUnsignedShort().let { if (it == 65535) -1 else it }
            modelZoom = stream.readUnsignedShort()
            modelXAngle = stream.readUnsignedShort()
            modelYAngle = stream.readUnsignedShort()
        }
        if (type == ComponentType.INVENTORY_TEXT) {
            widthMode = 3
            heightMode = 3
            textHorizontalAli = stream.readUnsignedByte()
            fontId = stream.readUnsignedShort().let { if (it == 65535) -1 else it }
            shadow = stream.readUnsignedByte() == 1
            color = stream.readInt()
            invMarginX = stream.readShort()
            invMarginY = stream.readShort()
            if1Type7HasOptions = stream.readUnsignedByte()
            if (if1Type7HasOptions == 1) flags = flags or 0x40000000
            invOptions = arrayOfNulls(5)
            for (i in 0 until 5) {
                val o = Utils.getJagString(stream)
                if (o.isNotEmpty()) {
                    invOptions!![i] = o
                    flags = flags or (1 shl (i + 23))
                }
            }
        }
        if (type == ComponentType.TOOLTIP) {
            text = Utils.getJagString(stream)
        }
        if (buttonType == 2 || type == ComponentType.INVENTORY) {
            optionCircumfix = Utils.getJagString(stream)
            optionSuffix = Utils.getJagString(stream)
            if1Mask = stream.readUnsignedShort()
            flags = flags or ((if1Mask and 0x3F) shl 11)
        }
        if (buttonType == 1 || buttonType == 4 || buttonType == 5 || buttonType == 6) {
            option = Utils.getJagString(stream)
        }
        if (buttonType == 1 || buttonType == 4 || buttonType == 5) flags = flags or 0x400000
        if (buttonType == 6) flags = flags or 0x1
        optionMask = flags
        settings = IComponentSettings(optionMask, mask)
    }

    fun decodeIf3(
        stream: InputStream,
        componentId: Int,
        interfaceId: Int,
    ) {
        stream.readUnsignedByte()
        version = 3

        this.componentId = componentId
        this.interfaceId = interfaceId

        var rawType = stream.readUnsignedByte()
        if ((rawType and 0x80) != 0) {
            rawType = rawType and 0x7F
            name = Utils.getJagString(stream)
        }
        type = rawType

        contentType = stream.readUnsignedShort()
        baseX = stream.readShort()
        baseY = stream.readShort()
        baseWidth = stream.readUnsignedShort()
        baseHeight = stream.readUnsignedShort()
        widthMode = stream.readByte().toByte()
        heightMode = stream.readByte().toByte()
        yMode = stream.readByte().toByte()
        xMode = stream.readByte().toByte()

        val rawParent = stream.readUnsignedShort()
        parentId = if (rawParent == 65535) -1 else (interfaceId shl 16) or rawParent

        hidden = stream.readUnsignedByte() == 1

        when (type) {
            ComponentType.LAYER -> {
                scrollWidth = stream.readUnsignedShort()
                scrollHeight = stream.readUnsignedShort()
                noClickThrough = stream.readUnsignedByte() == 1
            }
            ComponentType.SPRITE -> {
                spriteId = stream.readInt()
                angle2d = stream.readUnsignedShort()
                val flags = stream.readUnsignedByte()
                spriteTiling = (flags and 0x1) != 0
                hasAlpha = (flags and 0x2) != 0
                transparency = stream.readUnsignedByte()
                borderThickness = stream.readUnsignedByte()
                shadowColor = stream.readInt()
                vFlip = stream.readUnsignedByte() == 1
                hFlip = stream.readUnsignedByte() == 1
            }
            ComponentType.MODEL -> {
                modelType = 1
                modelId = stream.readUnsignedShort().let { if (it == 65535) -1 else it }
                modelOriginX = stream.readShort()
                modelOriginY = stream.readShort()
                modelXAngle = stream.readUnsignedShort()
                modelYAngle = stream.readUnsignedShort()
                modelZAngle = stream.readUnsignedShort()
                modelZoom = stream.readUnsignedShort()
                modelAnimId = stream.readUnsignedShort().let { if (it == 65535) -1 else it }
                modelOrtho = stream.readUnsignedByte() == 1
                unknownModelProp3 = stream.readUnsignedShort()
                unknownModelProp4 = stream.readUnsignedShort()
                unknownModelProp5 = stream.readUnsignedByte() == 1
                if (widthMode.toInt() != 0) modelObjWidth = stream.readUnsignedShort()
                if (heightMode.toInt() != 0) modelObjHeight = stream.readUnsignedShort()
            }
            ComponentType.TEXT -> {
                fontId = stream.readUnsignedShort().let { if (it == 65535) -1 else it }
                text = Utils.getJagString(stream)
                textLineHeight = stream.readUnsignedByte()
                textHorizontalAli = stream.readUnsignedByte()
                textVerticalAli = stream.readUnsignedByte()
                shadow = stream.readUnsignedByte() == 1
                color = stream.readInt()
            }
            ComponentType.FIGURE -> {
                color = stream.readInt()
                filled = stream.readUnsignedByte() == 1
                transparency = stream.readUnsignedByte()
            }
            ComponentType.LINE -> {
                lineWidth = stream.readUnsignedByte()
                color = stream.readInt()
                unknownProp_8 = stream.readUnsignedByte() == 1
            }
        }

        optionMask = stream.read24BitInt()
        var key = stream.readUnsignedByte()

        if (key != 0) {
            opId = IntArray(10)
            repeatCount = ByteArray(10)
            modifier = ByteArray(10)

            while (key != 0) {
                val index = (key shr 4) - 1
                var value = ((key and 0xF) shl 8) or stream.readUnsignedByte()
                if (value == 4095) value = -1
                repeatCount!![index] = stream.readByte().toByte()
                modifier!![index] = stream.readByte().toByte()
                opId!![index] = value
                key = stream.readUnsignedByte()
            }
        }

        optionBase = Utils.getJagString(stream)
        val flags = stream.readUnsignedByte()
        val optionCount = flags and 0xF
        val cursorFlag = flags shr 4

        if (optionCount > 0) {
            rightClickOptions = arrayOfNulls(optionCount)
            repeat(optionCount) { rightClickOptions!![it] = Utils.getJagString(stream) }
        }

        if (cursorFlag > 0) {
            val index = stream.readUnsignedByte()
            opCursors = IntArray(index + 1) { -1 }
            opCursors!![index] = stream.readUnsignedShort()
            if (cursorFlag > 1) {
                val index2 = stream.readUnsignedByte()
                opCursors!![index2] = stream.readUnsignedShort()
            }
        } else {
            opCursors = null
        }

        dragDeadzone = stream.readUnsignedByte()
        dragDeadtime = stream.readUnsignedByte()
        dragRenderBehavior = stream.readUnsignedByte() == 1
        optionCircumfix = Utils.getJagString(stream)

        if ((optionMask shr 11 and 0x7F) != 0) {
            mask = stream.readUnsignedShort().let { if (it == 65535) -1 else it }
            targetOverCursor = stream.readUnsignedShort().let { if (it == 65535) -1 else it }
            targetLeaveCursor = stream.readUnsignedShort().let { if (it == 65535) -1 else it }
        }

        settings = IComponentSettings(optionMask, mask)

        onLoadScript = decodeScript(stream)
        onMouseHoverScript = decodeScript(stream)
        onMouseLeaveScript = decodeScript(stream)
        onUseWith = decodeScript(stream)
        onUse = decodeScript(stream)
        onVarpTransmit = decodeScript(stream)
        onInvTransmit = decodeScript(stream)
        onStatTransmit = decodeScript(stream)
        onTimer = decodeScript(stream)
        onOptionClick = decodeScript(stream)
        onMouseRepeat = decodeScript(stream)
        onClickRepeat = decodeScript(stream)
        onDrag = decodeScript(stream)
        onRelease = decodeScript(stream)
        onHold = decodeScript(stream)
        onDragStart = decodeScript(stream)
        onDragRelease = decodeScript(stream)
        onScroll = decodeScript(stream)
        onVarcTransmit = decodeScript(stream)
        onVarcStrTransmit = decodeScript(stream)

        varpTriggers = decodeTriggers(stream)
        inventoryTriggers = decodeTriggers(stream)
        statTriggers = decodeTriggers(stream)
        varcTriggers = decodeTriggers(stream)
        varcstrTriggers = decodeTriggers(stream)
    }

    val isModelOrtho: Boolean
        get() = modelOrtho

    private fun encodeScript(
        script: Array<Any?>?,
        out: OutputStream,
    ) {
        if (script.isNullOrEmpty()) {
            out.writeByte(0)
            return
        }

        out.writeByte(script.size)

        for (obj in script) {
            when (obj) {
                is Int -> {
                    out.writeByte(0)
                    out.writeInt(obj)
                }
                is String -> {
                    out.writeByte(1)
                    out.writeString(obj)
                }
                null -> {
                    out.writeByte(0)
                    out.writeInt(0)
                }
                else -> {
                    throw IllegalArgumentException("Unsupported script argument type: ${obj::class}")
                }
            }
        }
    }

    private fun decodeScript(buffer: InputStream): Array<Any?>? {
        val length = buffer.readUnsignedByte()
        if (length == 0) return null

        val objects = arrayOfNulls<Any>(length)

        for (i in 0 until length) {
            val type = buffer.readUnsignedByte()
            objects[i] =
                when (type) {
                    0 -> buffer.readInt()
                    1 -> Utils.getJagString(buffer)
                    else -> null
                }
        }

        hasScripts = true
        return objects
    }

    private fun decodeTriggers(buffer: InputStream): IntArray? {
        val length = buffer.readUnsignedByte()
        if (length == 0) {
            return null
        }
        val `is` = IntArray(length)
        for (index in 0 until length) {
            `is`[index] = buffer.readInt()
        }
        return `is`
    }

    private fun encodeTriggers(
        arr: IntArray?,
        out: OutputStream,
    ) {
        val length = arr?.size ?: 0
        out.writeByte(length)
        for (index in 0 until length) {
            out.writeInt(arr!![index])
        }
    }

    fun buildOpcodeComponentMap(components: List<ComponentDefinition>): Map<Int, Array<ComponentDefinition?>> {
        val grouped = components.groupBy { it.interfaceId }

        val map = mutableMapOf<Int, Array<ComponentDefinition?>>()
        for ((ifaceId, compList) in grouped) {
            val maxId = compList.maxOfOrNull { it.componentId } ?: 0
            val arr = arrayOfNulls<ComponentDefinition>(maxId + 1)
            for (comp in compList) {
                arr[comp.componentId] = comp
            }
            map[ifaceId] = arr
        }

        return map
    }

    init {
        componentHash = -1
        contentType = 0
        parentId = -1
        hidden = false
        noClickThrough = false

        baseX = 0
        baseY = 0
        baseWidth = 0
        baseHeight = 0
        modelObjWidth = 0
        modelObjHeight = 0
        xMode = 0
        yMode = 0
        widthMode = 0.toByte()
        heightMode = 0.toByte()

        color = 0

        borderThickness = 0
        spriteId = -1
        spriteTiling = false
        hasAlpha = false
        hFlip = false
        vFlip = false
        shadow = false
        filled = false

        modelId = -1
        modelAnimId = -1
        modelXAngle = 0
        modelYAngle = 0
        modelZAngle = 0
        modelZoom = 100
        modelOrtho = false
        modelOriginX = 0
        modelOriginY = 0
        unknownModelProp3 = 0
        unknownModelProp4 = 0
        unknownModelProp5 = false

        text = ""
        fontId = -1
        textHorizontalAli = 0
        textVerticalAli = 0
        textLineHeight = 0

        scrollWidth = 0
        scrollHeight = 0
        targetOverCursor = -1
        targetLeaveCursor = -1
        mouseOverCursor = -1

        dragDeadzone = 0
        dragDeadtime = 0
        dragRenderBehavior = false

        optionMask = 0
        optionBase = ""
        optionCircumfix = ""

        onLoadScript = null
        onMouseHoverScript = null
        onMouseLeaveScript = null
        onUseWith = null
        onUse = null
        onVarpTransmit = null
        onInvTransmit = null
        onStatTransmit = null
        onTimer = null
        onOptionClick = null
        onMouseRepeat = null
        onClickRepeat = null
        onDrag = null
        onRelease = null
        onHold = null
        onDragStart = null
        onDragRelease = null
        onScroll = null
        onVarcTransmit = null
        onVarcStrTransmit = null

        varpTriggers = null
        inventoryTriggers = null
        statTriggers = null
        varcTriggers = null
        varcstrTriggers = null
    }

    companion object {
        var componentDefinition: Array<Array<ComponentDefinition?>?>? =
            arrayOfNulls(getInterfaceDefinitionsSize())

        @JvmStatic
        fun getInterfaceComponent(interfaceId: Int, componentId: Int): ComponentDefinition? {
            val inter = getInterface(interfaceId) ?: return null
            if (componentId < 0 || componentId >= inter.size) return null
            return inter[componentId]
        }

        @JvmStatic
        fun getInterface(id: Int): Array<ComponentDefinition?>? = getInterface(id, false)

        @JvmStatic
        fun getInterface(id: Int, reload: Boolean): Array<ComponentDefinition?>? {
            if (componentDefinition == null || id >= componentDefinition!!.size) return null

            if (componentDefinition!![id] == null || reload) {
                val size = getInterfaceDefinitionsComponentsSize(id)
                componentDefinition!![id] = arrayOfNulls(size)

                for (i in 0 until size) {
                    val data: ByteArray? = Cache.store!!.indexes[3].getFile(id, i)
                    if (data == null) {
                        println("[$id]:[$i] = null")
                        continue
                    }

                    val defs = ComponentDefinition()
                    defs.componentHash = i + (id shl 16)
                    try {
                        defs.decode(InputStream(data), i, id)
                    } catch (e: Exception) {
                        println("[$id]:[$i] failed to decode: $e")
                        continue
                    }
                    componentDefinition!![id]!![i] = defs
                }
            }

            return componentDefinition!![id]
        }

        @JvmStatic
        fun getInterfaceDefinitionsSize(): Int = Cache.store!!.indexes[3].lastArchiveId + 1

        @JvmStatic
        fun getInterfaceDefinitionsComponentsSize(interfaceId: Int): Int =
            Cache.store!!.indexes[3].getLastFileId(interfaceId) + 1

        @JvmStatic
        fun getChildsByParent(interfaceId: Int, hash: Int): ArrayList<ComponentDefinition> {
            val allComponents = getInterface(interfaceId, false)
            val foundChildren = ArrayList<ComponentDefinition>()
            if (allComponents != null) {
                for (component in allComponents) {
                    if (component != null && component.parentId == hash) {
                        foundChildren.add(component)
                    }
                }
            }
            return foundChildren
        }

        @JvmStatic
        fun hasChilds(interfaceId: Int, parentHash: Int): Boolean {
            val list = getInterface(interfaceId) ?: return false
            for (c in list) {
                if (c != null && c.parentId == parentHash) return true
            }
            return false
        }

        @JvmStatic
        fun getInterfaceContainers(interfaceId: Int): ArrayList<ComponentDefinition>? {
            val possibleParents = getInterface(interfaceId) ?: return null
            val containers = ArrayList<ComponentDefinition>()
            for (component in possibleParents) {
                if (component != null &&
                    component.type == ComponentType.LAYER &&
                    hasChilds(interfaceId, component.componentHash)
                ) {
                    containers.add(component)
                }
            }
            return containers
        }

        fun copy(source: ComponentDefinition): ComponentDefinition {
            val bytes = source.encode()

            return ComponentDefinition().apply {
                decode(
                    InputStream(bytes),
                    source.componentId,
                    source.interfaceId
                )
            }
        }
    }
}
