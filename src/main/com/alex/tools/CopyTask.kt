package com.alex.tools

import com.alex.loaders.interfaces.ComponentDefinition
import java.util.function.Consumer

class CopyTask internal constructor(private val parent: Copy, val sourceId: Int, val targetId: Int) {
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

    fun x(x: Int): CopyTask {
        return modify { c: ComponentDefinition -> c.baseX = x }
    }

    fun y(y: Int): CopyTask {
        return modify { c: ComponentDefinition -> c.baseY = y }
    }

    fun size(w: Int, h: Int): CopyTask {
        return modify { c: ComponentDefinition ->
            c.baseWidth = w
            c.baseHeight = h
        }
    }

    fun parent(parentId: Int): CopyTask {
        return modify { c: ComponentDefinition -> c.parentId = parentId }
    }

    fun hidden(`val`: Boolean): CopyTask {
        return modify { cd: ComponentDefinition -> cd.hidden = `val` }
    }

    fun shadow(`val`: Boolean): CopyTask {
        return modify { cd: ComponentDefinition -> cd.shadow = `val` }
    }

    fun vFlip(`val`: Boolean): CopyTask {
        return modify { cd: ComponentDefinition -> cd.vFlip = `val` }
    }

    fun hFlip(`val`: Boolean): CopyTask {
        return modify { cd: ComponentDefinition -> cd.hFlip = `val` }
    }

    fun alpha(`val`: Boolean): CopyTask {
        return modify { cd: ComponentDefinition -> cd.hasAlpha = `val` }
    }

    fun filled(`val`: Boolean): CopyTask {
        return modify { cd: ComponentDefinition -> cd.filled = `val` }
    }

    fun noClickThrough(`val`: Boolean): CopyTask {
        return modify { cd: ComponentDefinition -> cd.noClickThrough = `val` }
    }

    fun spriteTiling(`val`: Boolean): CopyTask {
        return modify { cd: ComponentDefinition -> cd.spriteTiling = `val` }
    }

    fun hasScripts(`val`: Boolean): CopyTask {
        return modify { cd: ComponentDefinition -> cd.hasScripts = `val` }
    }

    fun positionX(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.positionX = `val` }
    }

    fun positionY(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.positionY = `val` }
    }

    fun width(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.width = `val` }
    }

    fun height(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.height = `val` }
    }

    fun parentId(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.parentId = `val` }
    }

    fun baseWidth(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.baseWidth = `val` }
    }

    fun targetOverCursor(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.targetOverCursor = `val` }
    }

    fun baseHeight(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.baseHeight = `val` }
    }

    fun borderThickness(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.borderThickness = `val` }
    }

    fun color(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.color = `val` }
    }

    fun transparency(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.transparency = `val` }
    }

    fun fontId(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.fontId = `val` }
    }

    fun mouseOverCursor(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.mouseOverCursor = `val` }
    }

    fun spriteId(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.spriteId = `val` }
    }

    fun zoom(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.modelZoom = `val` }
    }

    fun basePositionX(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.baseX = `val` }
    }

    fun basePositionY(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.baseY = `val` }
    }

    fun contentType(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.contentType = `val` }
    }

    fun Option(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.Option = `val` }
    }

    fun opMask(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.optionMask = `val` }
    }

    fun text(`val`: String?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.text = `val` }
    }

    fun name(`val`: String?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.name = `val` }
    }

    fun Name(`val`: String?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.optionBase = `val` }
    }

    fun mainOption(option: Int): CopyTask {
        return modify { c: ComponentDefinition -> c.Option = option }
    }

    fun targetLeaveCursor(`val`: Int): CopyTask {
        return modify { cd: ComponentDefinition -> cd.targetLeaveCursor = `val` }
    }

    fun opCursors(`val`: IntArray?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.opCursors = `val`!! }
    }

    fun varpTriggers(`val`: IntArray?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.varpTriggers = `val` }
    }

    fun inventoryTriggers(`val`: IntArray?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.inventoryTriggers = `val` }
    }

    fun statTriggers(`val`: IntArray?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.statTriggers = `val` }
    }

    fun varcTriggers(`val`: IntArray?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.varcTriggers = `val` }
    }

    fun varcstrTriggers(`val`: IntArray?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.varcstrTriggers = `val` }
    }

    fun onLoadScript(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onLoadScript = arrayOf(`val`) }
    }

    fun onMouseHoverScript(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onMouseHoverScript = arrayOf(`val`) }
    }

    fun onMouseLeaveScript(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onMouseLeaveScript = arrayOf(`val`) }
    }

    fun onUse(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onUse = arrayOf(`val`) }
    }

    fun onUseWith(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onUseWith = arrayOf(`val`) }
    }

    fun onVarpTransmit(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onVarpTransmit = arrayOf(`val`) }
    }

    fun onInvTransmit(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onInvTransmit = arrayOf(`val`) }
    }

    fun onStatTransmit(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onStatTransmit = arrayOf(`val`) }
    }

    fun onTimer(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onTimer = arrayOf(`val`) }
    }

    fun onOptionClick(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onOptionClick = arrayOf(`val`) }
    }

    fun onMouseRepeat(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onMouseRepeat = arrayOf(`val`) }
    }

    fun onClickRepeat(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onClickRepeat = arrayOf(`val`) }
    }

    fun onDrag(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onDrag = arrayOf(`val`) }
    }

    fun onRelease(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onRelease = arrayOf(`val`) }
    }

    fun onHold(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onHold = arrayOf(`val`) }
    }

    fun onDragStart(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onDragStart = arrayOf(`val`) }
    }

    fun onDragRelease(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onDragRelease = arrayOf(`val`) }
    }

    fun onScroll(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onScroll = arrayOf(`val`) }
    }

    fun onVarcTransmit(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onVarcTransmit = arrayOf(`val`) }
    }

    fun onVarcStrTransmit(vararg `val`: Any?): CopyTask {
        return modify { cd: ComponentDefinition -> cd.onVarcStrTransmit = arrayOf(`val`) }
    }

    fun save(): Copy {
        parent.save()
        return parent
    }
}
