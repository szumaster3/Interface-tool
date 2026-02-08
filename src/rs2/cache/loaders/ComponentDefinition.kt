package rs2.cache.loaders

import com.alex.io.InputStream
import com.alex.io.OutputStream
import com.alex.store.Index
import rs2.cache.Cache
import rs2.interfaces.ComponentConstants
import rs2.interfaces.InterfaceUtils.getParent
import rs2.interfaces.draw.ComponentPosition.setValues
import rs2.logging.LogFactory.createLogger
import java.util.*
import kotlin.math.max

class ComponentDefinition {
    var version: Byte = 0
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

    var width2: Int = 0
    var height2: Int = 0

    var dynamicWidth: Int = 0
    var dynamicHeight: Int = 0
    var vPadding: Int = 0
    var scrollMaxV: Int = 0
    var scrollMaxH: Int = 0
    var realX: Int = 0
    var realY: Int = 0
    var modelYOffset: Int = 0
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

    @JvmField public var vFlip: Boolean = false
    @JvmField public var hFlip: Boolean = false

    var text: String? = ""
    var textVerticalAli: Int = 0
    var textHorizontalAli: Int = 0
    var name: String? = null
    var multiline: Boolean = false
    var fontId: Int = 0

    var modelId: Int = 0
    var modelZoom: Int = 100
    var modelSeqId: Int = 0

    var color: Int = 0
    var transparency: Int = 0
    var borderThickness: Int = 0
    var clickMask: Boolean = true
    var angle2d: Int = 0
    var aspectWidthType: Byte = 0
    var aspectHeightType: Byte = 0

    var rightclickOptions: Array<String?>? = null
    var optionBase: String? = ""
    var optionCircumfix: String? = ""
    var optionMask: Int = 0
    var Option: Int = 0
    private var mask = -1
    var menuOptionsCount: Int = 0

    var dragDeadtime: Int = 0
    var dragDeadzone: Int = 0

    var aBoolean4707: Boolean = false
    var aBoolean4723: Boolean = false
    var aBoolean4727: Boolean = false
    var aBoolean4730: Boolean = false
    var aBoolean4802: Boolean = false
    var aBoolean4865: Boolean = false

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

    var aHashTable4823: Hashtable<*, *>? = null
    var aWidget4836: ComponentDefinition? = null
    var opCursors: IntArray = intArrayOf()

    var flag: Int = 0
    var anInt4681: Int = 0
    var anInt4683: Int = 0
    var anInt4687: Int = 0
    var anInt4703: Int = 0
    var anInt4709: Int = 0
    var anInt4718: Int = 0
    var anInt4719: Int = 0
    var anInt4734: Int = 0
    var anInt4747: Int = 0
    var anInt4748: Int = 0
    var anInt4780: Int = 0
    var anInt4783: Int = 0
    var anInt4797: Int = 0
    var anInt4801: Int = 0
    var anInt4810: Int = 0
    var anInt4811: Int = 0
    var anInt4813: Int = 0
    var anInt4824: Int = 0
    var anInt4842: Int = 0
    var anInt4848: Int = 0

    private val hashInt1: ArrayList<Int?> = ArrayList()
    private val hashString2: ArrayList<String?> = ArrayList()
    private val hashLong1: ArrayList<Long> = ArrayList()
    private val hashLong2: ArrayList<Long?> = ArrayList()

    var modelAnimId: Int = 0
    var animated: Boolean = false
    var centreType: Boolean = false
    var ignoreZBuffer: Boolean = false

    var viewportX: Int = 0
    var viewportY: Int = 0
    var viewportZ: Int = 0

    var spritePitch: Int = 0
    var spriteRoll: Int = 0
    var spriteYaw: Int = 0
    var spriteScale: Int = 0

    var lineWidth: Byte = 0
    var unknownProp_8: Boolean = false

    var opId: IntArray? = null
    var repeatCount: ByteArray? = null
    var modifier: ByteArray? = null

    fun encode(hasScripts: Boolean): ByteArray {
        return encodeIf3()
    }

    fun encodeIf3(): ByteArray {
        val out = OutputStream()
        out.writeByte(if (version.toInt() == -1) 255 else version.toInt())

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
        out.writeByte(aspectWidthType.toInt())
        out.writeByte(aspectHeightType.toInt())
        out.writeByte(xMode.toInt())
        out.writeByte(yMode.toInt())

        out.writeShort(
            if (type != ComponentConstants.LAYER && parentId != -1)
                parentId and 0xFFFF
            else
                65535
        )

        var flags = 0
        if (hidden) flags = flags or 0x1
        if (noClickThrough) flags = flags or 0x2
        out.writeByte(flags)

        when (type) {
            ComponentConstants.LAYER -> {
                out.writeShort(scrollMaxH)
                out.writeShort(scrollMaxV)
                out.writeByte(if (noClickThrough) 1 else 0)
            }

            ComponentConstants.SPRITE -> {
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

            ComponentConstants.MODEL -> {
                out.writeShort(if (modelId == -1) 65535 else modelId)

                var modelFlags = 0
                if (animated) modelFlags = modelFlags or 0x4
                if (centreType) modelFlags = modelFlags or 0x2
                if (ignoreZBuffer) modelFlags = modelFlags or 0x8

                val simple =
                    viewportZ == 0 &&
                            (viewportX != 0 || viewportY != 0 ||
                                    spritePitch != 0 || spriteRoll != 0 ||
                                    spriteYaw != 0 || spriteScale != 100)

                if (simple) modelFlags = modelFlags or 0x1
                out.writeByte(modelFlags)

                if ((modelFlags and 0x1) != 0) {
                    out.writeShort(viewportX)
                    out.writeShort(viewportY)
                    out.writeShort(spritePitch)
                    out.writeShort(spriteRoll)
                    out.writeShort(spriteYaw)
                    out.writeShort(spriteScale)
                } else {
                    out.writeShort(viewportX)
                    out.writeShort(viewportY)
                    if (centreType) out.writeShort(viewportZ)
                    out.writeShort(spritePitch)
                    out.writeShort(spriteRoll)
                    out.writeShort(spriteYaw)
                    out.writeShort(spriteScale)
                }

                out.writeShort(if (modelAnimId == -1) 65535 else modelAnimId)
                if (aspectWidthType.toInt() != 0 && dynamicWidth != 0)
                    out.writeShort(dynamicWidth)

                if (aspectHeightType.toInt() != 0 && dynamicHeight != 0)
                    out.writeShort(dynamicHeight)
            }

            ComponentConstants.TEXT -> {
                out.writeShort(if (fontId == -1) 65535 else fontId)
                out.writeString(text ?: "")
                out.writeByte(vPadding)
                out.writeByte(textHorizontalAli)
                out.writeByte(textVerticalAli)
                out.writeByte(if (shadow) 1 else 0)
                out.writeInt(color)
                out.writeByte(transparency)
                out.writeByte(if (multiline) 1 else 0)
            }

            ComponentConstants.FIGURE -> {
                out.writeInt(color)
                out.writeByte(if (filled) 1 else 0)
                out.writeByte(transparency)
            }

            ComponentConstants.UNKNOWN -> {
                out.writeByte(lineWidth.toInt())
                out.writeInt(color)
                out.writeByte(if (unknownProp_8) 1 else 0)
            }
        }

        out.write24BitInt(optionMask)

        if (opId == null) {
            out.writeByte(0)
        } else {
            for (i in opId!!.indices) {
                val v = if (opId!![i] == -1) 4095 else opId!![i]
                out.writeByte(((i + 1) shl 4) or (v shr 8))
                out.writeByte(v and 0xFF)
                out.writeByte(repeatCount?.getOrNull(i)?.toInt() ?: 0)
                out.writeByte(modifier?.getOrNull(i)?.toInt() ?: 0)
            }
            out.writeByte(0)
        }

        out.writeString(optionBase ?: "")

        val optionCount = rightclickOptions?.size ?: 0
        val cursorCount = opCursors?.size ?: 0
        out.writeByte((cursorCount shl 4) or optionCount)

        if (optionCount > 0)
            for (s in rightclickOptions!!)
                out.writeString(s ?: "")

        if (cursorCount > 0)
            for (i in 0 until cursorCount)
                out.writeShort(opCursors!![i])

        out.writeByte(dragDeadzone)
        out.writeByte(dragDeadtime)
        out.writeByte(if (dragRenderBehavior) 1 else 0)
        out.writeString(optionCircumfix ?: "")

        if ((optionMask shr 11 and 0x7F) != 0) {
            out.writeShort(mask)
            out.writeShort(targetOverCursor)
            out.writeShort(targetLeaveCursor)
        }
        settings= IComponentSettings(optionMask,mask)
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

        val data = ByteArray(out.offset)
        out.offset = 0
        out.getBytes(data, 0, data.size)
        return data
    }

    private fun encodeIf1(): ByteArray? {

        return null
    }

    fun decode(stream: InputStream, componentId: Int, interfaceId: Int) {
        version = stream.readUnsignedByte().toByte()
        if (version.toInt() == 255) version = -1

        this.componentId = componentId
        this.interfaceId = interfaceId

        type = stream.readUnsignedByte()
        if ((type and 0x80) != 0) {
            type = type and 0x7F
            name = stream.readString()
        }

        contentType = stream.readUnsignedShort()
        baseX = stream.readShort()
        baseY = stream.readShort()
        baseWidth = stream.readUnsignedShort()
        baseHeight = stream.readUnsignedShort()
        aspectWidthType = stream.readByte().toByte()
        aspectHeightType = stream.readByte().toByte()
        xMode = stream.readByte().toByte()
        yMode = stream.readByte().toByte()

        val rawParent = stream.readUnsignedShort()
        parentId = if ((rawParent == 65535)) -1 else (interfaceId shl 16) or rawParent

        val flags = stream.readUnsignedByte()
        hidden = (flags and 0x1) != 0
        noClickThrough = (flags and 0x2) != 0

        when (type) {
            ComponentConstants.LAYER -> {
                scrollMaxH = stream.readUnsignedShort()
                scrollMaxV = stream.readUnsignedShort()
                noClickThrough = stream.readUnsignedByte() == 1
            }
            ComponentConstants.SPRITE -> {
                spriteId = stream.readInt()
                angle2d = stream.readUnsignedShort()
                val spriteFlags = stream.readUnsignedByte()
                spriteTiling = (spriteFlags and 0x1) != 0
                hasAlpha = (spriteFlags and 0x2) != 0
                transparency = stream.readUnsignedByte()
                borderThickness = stream.readUnsignedByte()
                shadowColor = stream.readInt()
                vFlip = stream.readUnsignedByte() == 1
                hFlip = stream.readUnsignedByte() == 1
            }
            ComponentConstants.MODEL -> {
                modelId = stream.readUnsignedShort()
                if (modelId == 65535) modelId = -1

                val modelFlags = stream.readUnsignedByte()
                animated = (modelFlags and 0x4) != 0
                centreType = (modelFlags and 0x2) != 0
                ignoreZBuffer = (modelFlags and 0x8) != 0

                if ((modelFlags and 0x1) != 0) {
                    viewportX = stream.readShort()
                    viewportY = stream.readShort()
                    spritePitch = stream.readShort()
                    spriteRoll = stream.readShort()
                    spriteYaw = stream.readShort()
                    spriteScale = stream.readShort()
                } else {
                    viewportX = stream.readShort()
                    viewportY = stream.readShort()
                    if (centreType) viewportZ = stream.readShort()
                    spritePitch = stream.readShort()
                    spriteRoll = stream.readShort()
                    spriteYaw = stream.readShort()
                    spriteScale = stream.readShort()
                }

                modelAnimId = stream.readUnsignedShort()
                if (modelAnimId == 65535) modelAnimId = -1

                if (aspectWidthType.toInt() != 0) dynamicWidth = stream.readUnsignedShort()
                if (aspectHeightType.toInt() != 0) dynamicHeight = stream.readUnsignedShort()
            }
            ComponentConstants.TEXT -> {
                fontId = stream.readUnsignedShort()
                if (fontId == 65535) fontId = -1
                text = stream.readString()
                vPadding = stream.readUnsignedByte()
                textHorizontalAli = stream.readUnsignedByte()
                textVerticalAli = stream.readUnsignedByte()
                shadow = stream.readUnsignedByte() == 1
                color = stream.readInt()
                transparency = stream.readUnsignedByte()
                multiline = stream.readUnsignedByte() == 1
            }
            ComponentConstants.FIGURE -> {
                color = stream.readInt()
                filled = stream.readUnsignedByte() == 1
                transparency = stream.readUnsignedByte()
            }
            ComponentConstants.UNKNOWN -> {
                lineWidth = stream.readUnsignedByte().toByte()
                color = stream.readInt()
                unknownProp_8 = stream.readUnsignedByte() == 1
            }
        }
        optionMask = stream.read24BitInt()
        var key = stream.readUnsignedByte()
        if (key != 0) {
            opId = IntArray(11)
            repeatCount = ByteArray(11)
            modifier = ByteArray(11)

            while (key != 0) {
                val index = (key shr 4) - 1
                var value = ((key and 0xF) shl 8) or stream.readUnsignedByte()
                if (value == 4095) value = -1

                val repeat = stream.readByte().toByte()
                val modifier = stream.readByte().toByte()

                if (index >= 0 && index < 11) {
                    opId!![index] = value
                    repeatCount!![index] = repeat
                    this.modifier!![index] = modifier
                }
                key = stream.readUnsignedByte()
            }
        }

        optionBase = getString(stream)

        val optionFlags = stream.readUnsignedByte()
        val optionCount = optionFlags and 0xF
        val cursorCount = optionFlags shr 4

        if (optionCount > 0) {
            rightclickOptions = arrayOfNulls(optionCount)
            for (i in 0 until optionCount) rightclickOptions!![i] = getString(stream)
        }

        if (cursorCount > 0) {
            opCursors = IntArray(cursorCount)
            for (i in 0 until cursorCount) opCursors[i] = stream.readUnsignedShort()
        }

        dragDeadzone = stream.readUnsignedByte()
        dragDeadtime = stream.readUnsignedByte()
        dragRenderBehavior = stream.readUnsignedByte() == 1
        optionCircumfix = getString(stream)

        if ((optionMask shr 11 and 0x7F) != 0) {
            mask = stream.readUnsignedShort()
            targetOverCursor = stream.readUnsignedShort()
            targetLeaveCursor = stream.readUnsignedShort()
        }

        settings= IComponentSettings(optionMask,mask)

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

    private fun encodeScript(script: Array<Any?>?, out: OutputStream) {
        if (script.isNullOrEmpty()) {
            out.writeByte(0)
            return
        }
        out.writeByte(script.size)
        for (obj in script) {
            if (obj is String) {
                out.writeByte(1)
                out.writeString(obj as String?)
            } else if (obj is Int) {
                out.writeByte(0)
                out.writeInt((obj as Int?)!!)
            }
        }
    }

    private fun decodeScript(buffer: InputStream): Array<Any?>? {
        val length = buffer.readUnsignedByte()
        if (length == 0) return null

        val objects = arrayOfNulls<Any>(length)

        for (index in 0 until length) {
            val type = buffer.readUnsignedByte()
            objects[index] =
                when (type) {
                    0 -> buffer.readInt()
                    1 -> getString(buffer)
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

    private fun encodeTriggers(arr: IntArray?, out: OutputStream) {
        val length = arr?.size ?: 0
        out.writeByte(length)
        for (index in 0 until length) {
            out.writeInt(arr!![index])
        }
    }

    fun getString(stream: InputStream): String {
        val s = StringBuilder()
        var b: Int
        while ((stream.readByte().also { b = it }) != 0) {
            s.append(b.toChar())
        }
        return s.toString()
    }

    init {
        targetOverCursor = -1
        fontId = -1
        aspectWidthType = 0.toByte()
        mouseOverCursor = -1
        aspectHeightType = 0.toByte()
        anInt4687 = -1
        anInt4783 = -1
        modelSeqId = -1
        componentHash = -1
        anInt4718 = -1
        spriteId = -1
        xMode = 0.toByte()
        anInt4748 = -1
        settings = IComponentSettings(mask, -1)
        contentType = 0
        anInt4811 = 0
        baseHeight = 0
        dynamicHeight = 0
        anInt4683 = -1
        parentId = -1
        anInt4801 = 0
        anInt4824 = -1
        aWidget4836 = null
        anInt4703 = -1
        noClickThrough = false
        hidden = false
        baseX = 0
        spriteTiling = false
        anInt4848 = -1
        anInt4797 = 0
        dragRenderBehavior = false
        aBoolean4723 = false
        yMode = 0.toByte()
        baseY = 0
        anInt4842 = 0
        targetLeaveCursor = -1
    }

    companion object {
        private val logger = createLogger(ComponentDefinition::class.java.name)
        var componentDefinition: Array<Array<ComponentDefinition?>?>? = arrayOfNulls<Array<ComponentDefinition?>?>(interfaceDefinitionsSize)
        private val storeIndex = Cache.store!!.indexes[3]

        @JvmStatic
        fun getInterface(id: Int, reload: Boolean): Array<ComponentDefinition?>? {
            if (componentDefinition == null) {
                componentDefinition = arrayOfNulls<Array<ComponentDefinition?>?>(storeIndex.lastArchiveId + 1)
            }

            if (id < 0 || id >= componentDefinition!!.size) {
                logger.warning("Interface id $id is out of bounds.")
                return null
            }

            if (componentDefinition!![id] != null && !reload) {
                return componentDefinition!![id]
            }

            val filesCount = storeIndex!!.getValidFilesCount(id)
            if (filesCount <= 0) return null

            val if3Components = mutableListOf<ComponentDefinition?>()

            for (i in 0 until filesCount) {
                val data = storeIndex.getFile(id, i) ?: continue
                val version = data[0].toInt() and 0xFF
                if (version != 255) continue

                val defs = ComponentDefinition()
                defs.componentHash = (id shl 16) or i
                try {
                    defs.decode(InputStream(data), i, id)
                    if3Components.add(defs)
                } catch (_: Exception) {
                    continue
                }
            }

            if (if3Components.isEmpty()) return null

            componentDefinition!![id] = if3Components.toTypedArray()
            return componentDefinition!![id]
        }

        @JvmStatic
        fun getInterfaceDefinitionsComponentsSize(id: Int): Int {
            if (storeIndex == null || !storeIndex.archiveExists(id)) {
                return 0
            }
            return max(storeIndex.getValidFilesCount(id), 0)
        }

        val interfaceDefinitionsSize: Int
            get() {
                if (Cache.store == null || Cache.store!!.indexes == null || Cache.store!!.indexes.size <= 3) {
                    return 0
                }
                return Cache.store!!.indexes[3].lastArchiveId + 1
            }

        @JvmStatic
        fun getInterfaceComponent(interfaceId: Int, componentId: Int): ComponentDefinition? {
            val components = getInterface(interfaceId, false)
            if (components == null || componentId < 0 || componentId >= components.size) {
                return null
            }
            return components[componentId]
        }

        fun method925(optionMask: Int): Int {
            return (optionMask and 0x3FDA8) shr 11
        }

        @JvmStatic
        fun getChildsByParent(interfaceId: Int, hash: Int): ArrayList<ComponentDefinition> {
            val allComponents = getInterface(interfaceId, false)
            val foundChilderen = ArrayList<ComponentDefinition>()
            for (component in allComponents!!) {
                if (component == null) continue
                if (hash == component.parentId) foundChilderen.add(component)
            }
            return foundChilderen
        }

        @JvmStatic
        fun hasChilds(interfaceId: Int, parentHash: Int): Boolean {
            val list = getInterface(interfaceId, false)
            for (c in list!!) {
                if (c == null) continue
                if (c.parentId == parentHash) return true
            }
            return false
        }

        @JvmStatic
        fun getInterfaceContainers(interfaceId: Int): ArrayList<ComponentDefinition>? {
            val possibleParents = getInterface(interfaceId, false)
            val containers = ArrayList<ComponentDefinition>()
            if (possibleParents == null) return null
            for (component in possibleParents) {
                if (component == null) continue
                if (component.type == ComponentConstants.LAYER && hasChilds(interfaceId, component.componentHash)) {
                    containers.add(component)
                }
            }

            return containers
        }

        @JvmStatic
        fun getX(c: ComponentDefinition, inter: Int): Int {
            if (c.parentId == -1) return c.positionX
            setValues(c)
            var parent = getParent(c.parentId)
            var positionX = c.positionX
            while (parent != null) {
                positionX += parent.positionX
                parent = getParent(parent.parentId)
            }
            return positionX
        }

        @JvmStatic
        fun getY(c: ComponentDefinition, inter: Int): Int {
            if (c.parentId == -1) return c.positionY
            setValues(c)
            var parent = getParent(c.parentId)
            var positionY = c.positionY
            while (parent != null) {
                positionY += parent.positionY
                parent = getParent(parent.parentId)
            }

            return positionY
        }
    }
}
