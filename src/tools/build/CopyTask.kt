package tools.build

import rs2.cache.loaders.ComponentDefinition
import java.util.function.Consumer

class CopyTask internal constructor(
    private val parent: Copy,
    val sourceId: Int,
    val targetId: Int,
) {
    var modifier: Consumer<ComponentDefinition>? = null

    internal constructor(parent: Copy, targetId: Int) : this(parent, -1, targetId)

    fun modify(modifier: Consumer<ComponentDefinition>): CopyTask {
        if (this.modifier == null) {
            this.modifier = modifier
        } else {
            val old = this.modifier
            this.modifier =
                Consumer<ComponentDefinition> { c: ComponentDefinition ->
                    old!!.accept(c)
                    modifier.accept(c)
                }
        }
        return this
    }

    fun x(x: Int): CopyTask = modify { c: ComponentDefinition -> c.baseX = x }

    fun y(y: Int): CopyTask = modify { c: ComponentDefinition -> c.baseY = y }

    fun size(
        w: Int,
        h: Int,
    ): CopyTask =
        modify { c: ComponentDefinition ->
            c.baseWidth = w
            c.baseHeight = h
        }

    fun parent(parentId: Int): CopyTask = modify { c: ComponentDefinition -> c.parentId = parentId }

    fun hidden(`val`: Boolean): CopyTask = modify { cd: ComponentDefinition -> cd.hidden = `val` }

    fun shadow(`val`: Boolean): CopyTask = modify { cd: ComponentDefinition -> cd.shadow = `val` }

    fun vFlip(`val`: Boolean): CopyTask = modify { cd: ComponentDefinition -> cd.vFlip = `val` }

    fun hFlip(`val`: Boolean): CopyTask = modify { cd: ComponentDefinition -> cd.hFlip = `val` }

    fun alpha(`val`: Boolean): CopyTask = modify { cd: ComponentDefinition -> cd.hasAlpha = `val` }

    fun clickMask(`val`: Boolean): CopyTask = modify { cd: ComponentDefinition -> cd.clickMask = `val` }

    fun filled(`val`: Boolean): CopyTask = modify { cd: ComponentDefinition -> cd.filled = `val` }

    fun noClickThrough(`val`: Boolean): CopyTask = modify { cd: ComponentDefinition -> cd.noClickThrough = `val` }

    fun aBoolean4707(`val`: Boolean): CopyTask = modify { cd: ComponentDefinition -> cd.aBoolean4707 = `val` }

    fun aBoolean4723(`val`: Boolean): CopyTask = modify { cd: ComponentDefinition -> cd.aBoolean4723 = `val` }

    fun aBoolean4727(`val`: Boolean): CopyTask = modify { cd: ComponentDefinition -> cd.aBoolean4727 = `val` }

    fun aBoolean4730(`val`: Boolean): CopyTask = modify { cd: ComponentDefinition -> cd.aBoolean4730 = `val` }

    fun aBoolean4802(`val`: Boolean): CopyTask = modify { cd: ComponentDefinition -> cd.aBoolean4802 = `val` }

    fun spriteTiling(`val`: Boolean): CopyTask = modify { cd: ComponentDefinition -> cd.spriteTiling = `val` }

    fun aBoolean4865(`val`: Boolean): CopyTask = modify { cd: ComponentDefinition -> cd.aBoolean4865 = `val` }

    fun hasScripts(`val`: Boolean): CopyTask = modify { cd: ComponentDefinition -> cd.hasScripts = `val` }

    fun positionX(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.positionX = `val` }

    fun positionY(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.positionY = `val` }

    fun width(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.width = `val` }

    fun height(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.height = `val` }

    fun realX(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.realX = `val` }

    fun anInt4681(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.anInt4681 = `val` }

    fun anInt4683(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.anInt4683 = `val` }

    fun anInt4687(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.anInt4687 = `val` }

    fun layerHeight(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.scrollMaxV = `val` }

    fun parentId(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.parentId = `val` }

    fun baseWidth(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.baseWidth = `val` }

    fun height2(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.height2 = `val` }

    fun targetOverCursor(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.targetOverCursor = `val` }

    fun anInt4703(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.anInt4703 = `val` }

    fun anInt4718(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.anInt4718 = `val` }

    fun anInt4719(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.anInt4719 = `val` }

    fun baseHeight(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.baseHeight = `val` }

    fun layerWidth(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.scrollMaxH = `val` }

    fun borderThickness(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.borderThickness = `val` }

    fun anInt4747(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.anInt4747 = `val` }

    fun anInt4748(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.anInt4748 = `val` }

    fun color(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.color = `val` }

    fun transparency(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.transparency = `val` }

    fun fontId(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.fontId = `val` }

    fun mouseOverCursor(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.mouseOverCursor = `val` }

    fun multiline(`val`: Boolean): CopyTask = modify { cd: ComponentDefinition -> cd.multiline = `val` }

    fun spriteId(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.spriteId = `val` }

    fun zoom(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.modelZoom = `val` }

    fun animationId(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.modelAnimId = `val` }

    fun basePositionX(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.baseX = `val` }

    fun basePositionY(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.baseY = `val` }

    fun contentType(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.contentType = `val` }

    fun Option(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.Option = `val` }

    fun opMask(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.optionMask = `val` }

    fun menuOptionsCount(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.menuOptionsCount = `val` }

    fun flag(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.flag = `val` }

    fun text(`val`: String?): CopyTask = modify { cd: ComponentDefinition -> cd.text = `val` }

    fun name(`val`: String?): CopyTask = modify { cd: ComponentDefinition -> cd.name = `val` }

    fun Name(`val`: String?): CopyTask = modify { cd: ComponentDefinition -> cd.optionBase = `val` }

    fun rightClickOptions(vararg options: String?): CopyTask =
        modify { c: ComponentDefinition -> c.rightClickOptions = options as Array<String?> }

    fun mainOption(option: Int): CopyTask = modify { c: ComponentDefinition -> c.Option = option }

    fun targetLeaveCursor(`val`: Int): CopyTask = modify { cd: ComponentDefinition -> cd.targetLeaveCursor = `val` }

    fun opCursors(`val`: IntArray?): CopyTask = modify { cd: ComponentDefinition -> cd.opCursors = `val`!! }

    fun anIntArray4705(`val`: IntArray?): CopyTask = modify { cd: ComponentDefinition -> cd.opId = `val` }

    fun varpTriggers(`val`: IntArray?): CopyTask = modify { cd: ComponentDefinition -> cd.varpTriggers = `val` }

    fun inventoryTriggers(`val`: IntArray?): CopyTask = modify { cd: ComponentDefinition -> cd.inventoryTriggers = `val` }

    fun statTriggers(`val`: IntArray?): CopyTask = modify { cd: ComponentDefinition -> cd.statTriggers = `val` }

    fun varcTriggers(`val`: IntArray?): CopyTask = modify { cd: ComponentDefinition -> cd.varcTriggers = `val` }

    fun varcstrTriggers(`val`: IntArray?): CopyTask = modify { cd: ComponentDefinition -> cd.varcstrTriggers = `val` }

    fun onLoadScript(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onLoadScript = arrayOf(`val`) }

    fun onMouseHoverScript(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onMouseHoverScript = arrayOf(`val`) }

    fun onMouseLeaveScript(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onMouseLeaveScript = arrayOf(`val`) }

    fun onUse(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onUse = arrayOf(`val`) }

    fun onUseWith(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onUseWith = arrayOf(`val`) }

    fun onVarpTransmit(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onVarpTransmit = arrayOf(`val`) }

    fun onInvTransmit(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onInvTransmit = arrayOf(`val`) }

    fun onStatTransmit(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onStatTransmit = arrayOf(`val`) }

    fun onTimer(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onTimer = arrayOf(`val`) }

    fun onOptionClick(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onOptionClick = arrayOf(`val`) }

    fun onMouseRepeat(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onMouseRepeat = arrayOf(`val`) }

    fun onClickRepeat(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onClickRepeat = arrayOf(`val`) }

    fun onDrag(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onDrag = arrayOf(`val`) }

    fun onRelease(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onRelease = arrayOf(`val`) }

    fun onHold(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onHold = arrayOf(`val`) }

    fun onDragStart(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onDragStart = arrayOf(`val`) }

    fun onDragRelease(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onDragRelease = arrayOf(`val`) }

    fun onScroll(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onScroll = arrayOf(`val`) }

    fun onVarcTransmit(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onVarcTransmit = arrayOf(`val`) }

    fun onVarcStrTransmit(vararg `val`: Any?): CopyTask = modify { cd: ComponentDefinition -> cd.onVarcStrTransmit = arrayOf(`val`) }

    fun save(): Copy {
        parent.save()
        return parent
    }
}
