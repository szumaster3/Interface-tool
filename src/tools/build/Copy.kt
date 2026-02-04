package tools.build

import com.logging.LogFactory
import com.rs.cache.Cache
import com.rs.cache.loaders.ComponentDefinition
import ui.InterfaceUI
import java.util.function.Consumer
import java.util.logging.Logger

class Copy private constructor(val targetInterface: Int) {
    private val tasks: MutableList<CopyTask> = ArrayList()

    private var src = 0
    private var startId = 0
    private var currentId = 0

    private var modifier: Consumer<ComponentDefinition?>? = null

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

    fun save() {
        for (task in tasks) {
            var comp: ComponentDefinition?

            if (task.sourceId >= 0) {
                comp = ComponentDefinition.getInterfaceComponent(src, task.sourceId)
            } else {
                comp = createNewComponent(task.targetId)
                comp!!.parentId = -1
            }

            comp!!.componentHash = (targetInterface shl 16) or task.targetId

            if (modifier != null) {
                modifier!!.accept(comp)
            }

            if (task.modifier != null) {
                task.modifier!!.accept(comp)
            }

            Cache.store!!.indexes[3].putFile(targetInterface, task.targetId, comp.encode(comp.hasScripts))

            logger.info(
                "Copied: " + src + ":" + task.sourceId + " -> " + targetInterface + ":" + task.targetId
            )
        }

        ComponentDefinition.getInterface(targetInterface, true)
    }

    companion object {
        private val logger: Logger = LogFactory.createLogger(InterfaceUI::class.java.name)

        @JvmStatic
        fun to(targetInterface: Int): Copy {
            return Copy(targetInterface)
        }

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
            val newId = ComponentDefinition.interfaceDefinitionsSize
            val base = ComponentDefinition.getInterfaceComponent(templateInterface, templateComponent)

            if (base != null) {
                base.baseX = 0
            }
            if (base != null) {
                base.baseY = 0
            }
            if (base != null) {
                base.parentId = -1
            }

            if (base != null) {
                Cache.store!!.indexes[3].putFile(newId, 0, base.encode(base.hasScripts))
            }
            ComponentDefinition.componentDefinition = arrayOfNulls(newId + 1)
            logger.info("Created new interface: $newId")
            return newId
        }
    }
}
