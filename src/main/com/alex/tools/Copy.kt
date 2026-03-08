package com.alex.tools

import com.alex.Cache
import com.alex.loaders.interfaces.ComponentDefinition
import java.util.function.Consumer

class Copy private constructor(val targetInterface: Int) {
    private val tasks: MutableList<CopyTask> = ArrayList()

    private var src = 0
    private var startId = 0
    private var currentId = 0
    private var modifier: Consumer<ComponentDefinition?>? = null
    private val copiedComponents = mutableListOf<ComponentDefinition>()

    fun from(sourceInterface: Int): Copy {
        this.src = sourceInterface
        return this
    }

    fun startAt(id: Int): Copy {
        this.startId = id
        this.currentId = id
        return this
    }

    fun modify(modifier: Consumer<ComponentDefinition?>?): Copy {
        this.modifier = modifier
        return this
    }

    fun copy(sourceId: Int): CopyTask {
        val task = CopyTask(this, sourceId, currentId++)
        tasks.add(task)
        return task
    }

    fun copy(vararg sourceIds: Int): Copy {
        for (id in sourceIds) {
            tasks.add(CopyTask(this, id, currentId++))
        }
        return this
    }

    fun copyRange(from: Int, to: Int): Copy {
        for (i in from..to) {
            copy(i)
        }
        return this
    }

    fun addComponents(vararg modifiers: Consumer<ComponentDefinition>?): Copy {
        for (mod in modifiers) {
            val task = CopyTask(this, -1, currentId++)
            task.modifier = mod
            tasks.add(task)
        }
        return this
    }

    private fun createNewComponent(targetId: Int): ComponentDefinition? {
        var comps = ComponentDefinition.getInterface(targetInterface, true)
        if (comps!!.size <= targetId) {
            val expanded = arrayOfNulls<ComponentDefinition>(targetId + 1)
            System.arraycopy(comps, 0, expanded, 0, comps.size)
            ComponentDefinition.componentDefinition?.set(targetInterface, expanded)
            comps = expanded
        }
        if (comps[targetId] == null) {
            comps[targetId] = ComponentDefinition()
        }
        comps[targetId]!!.componentHash = (targetInterface shl 16) or targetId
        return comps[targetId]
    }

    fun save(): List<ComponentDefinition> {
        copiedComponents.clear()

        for (task in tasks) {
            var comp: ComponentDefinition?

            if (task.sourceId >= 0) {
                comp = ComponentDefinition.getInterfaceComponent(src, task.sourceId)
            } else {
                comp = createNewComponent(task.targetId)
                comp!!.parentId = -1
            }

            comp!!.componentHash = (targetInterface shl 16) or task.targetId

            modifier?.accept(comp)
            task.modifier?.accept(comp)

            Cache.getStore()!!.indexes[3].putFile(targetInterface, task.targetId, comp.encode(comp.hasScripts))

            copiedComponents.add(comp)
        }

        init(targetInterface)

        return copiedComponents
    }

    private fun init(targetId: Int) {
        val compDefs = ComponentDefinition.componentDefinition
        if (compDefs == null || compDefs.size <= targetId) {
            val newSize = targetId + 1
            val expanded = arrayOfNulls<Array<ComponentDefinition?>?>(newSize)
            compDefs?.copyInto(expanded)
            ComponentDefinition.componentDefinition = expanded
        }

        var comps = ComponentDefinition.componentDefinition!![targetId]
        val maxTarget = tasks.maxOfOrNull { it.targetId } ?: -1
        if (comps == null || comps.size <= maxTarget) {
            val newComps = arrayOfNulls<ComponentDefinition>(maxTarget + 1)
            comps?.copyInto(newComps)
            ComponentDefinition.componentDefinition!![targetId] = newComps
        }
    }

    fun getCopiedComponents(): List<ComponentDefinition> = copiedComponents

    companion object {

        @JvmStatic
        fun to(targetInterface: Int): Copy = Copy(targetInterface)

        @JvmStatic
        fun newInterface(): Copy {
            val id = createInterface(6, 36)
            return Copy(id)
        }

        @JvmStatic
        fun newInterface(templateInterface: Int, templateComponent: Int): Copy {
            val id = createInterface(templateInterface, templateComponent)
            return Copy(id)
        }

        @JvmStatic
        fun createInterface(templateInterface: Int, templateComponent: Int): Int {
            val newId = ComponentDefinition.getInterfaceDefinitionsSize()
            val base = ComponentDefinition.getInterfaceComponent(templateInterface, templateComponent)
            if (base != null) {
                base.baseX = 0
                base.baseY = 0
                base.parentId = -1
                Cache.getStore()!!.indexes[3].putFile(newId, 0, base.encode(base.hasScripts))
            }
            ComponentDefinition.componentDefinition = arrayOfNulls(newId + 1)
            return newId
        }
    }
}