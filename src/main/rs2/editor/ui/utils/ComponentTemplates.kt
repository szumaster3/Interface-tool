package rs2.editor.ui.utils

import com.alex.loaders.interfaces.ComponentDefinition
import com.alex.loaders.interfaces.ComponentDefinition.Companion.getInterface
import com.alex.loaders.interfaces.ComponentType
import com.alex.loaders.interfaces.IComponentSettings
import com.alex.tools.Copy
import rs2.editor.ui.InterfaceUI
import java.util.function.Consumer
import javax.swing.JOptionPane


object ComponentTemplates {

    @JvmStatic
    fun refreshUI(interfaceId: Int) {
        InterfaceUI.instance?.let { ui ->
            ui.loadComponents(interfaceId)
            ui.renderPanel.interfaceId = interfaceId
            ui.refreshTreeAndList()
            ui.renderPanel.repaint()
        }
    }

    @JvmStatic
    fun createTooltipButton() {
        val c = InterfaceUI.selectedComponent
        if (c == null || c.interfaceId == -1) {
            JOptionPane.showMessageDialog(
                InterfaceUI.instance,
                "Please select an interface from the list first",
                "Warning",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        val defaultId = ComponentDefinition.getInterfaceDefinitionsComponentsSize(c.interfaceId)
        val input = JOptionPane.showInputDialog(
            InterfaceUI.instance,
            "Save as? (Component ID)",
            defaultId
        )

        val compId = input?.toIntOrNull() ?: run {
            JOptionPane.showMessageDialog(
                InterfaceUI.instance,
                "Invalid Component ID",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
            return
        }

        Copy.to(c.interfaceId)
            .startAt(compId)
            .addComponents(Consumer { comp ->
                comp.version = 3
                comp.parentId = -1
                comp.type = ComponentType.SPRITE
                comp.baseX = 70
                comp.baseY = 70
                comp.baseWidth = 35
                comp.baseHeight = 35
                comp.spriteId = 1431
                comp.optionMask = 2
                comp.settings = IComponentSettings(2, -1)
                comp.rightClickOptions  = arrayOf("OP_NAME")
                comp.onMouseHoverScript = arrayOf(1357, -2147483645, 100)
                comp.onMouseLeaveScript = arrayOf(1501, -2147483645, "PARENT_ID")
                comp.onMouseRepeat      = arrayOf(38, -2147483645, 49938533, "TOOLTIP_TEXT", 25, 100)
            })
            .save()

        getInterface(c.interfaceId, true)
        refreshUI(c.interfaceId)
    }

    @JvmStatic
    fun createSpellButton() {
            val c = InterfaceUI.selectedComponent
            if (c == null || c.interfaceId == -1) {
                JOptionPane.showMessageDialog(
                    InterfaceUI.instance,
                    "Please select an interface from the list first",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE
                )
                return
            }

            val defaultId = ComponentDefinition.getInterfaceDefinitionsComponentsSize(c.interfaceId)
            val input = JOptionPane.showInputDialog(
                InterfaceUI.instance,
                "Save as? (Component ID)",
                defaultId
            )

            val compId = input?.toIntOrNull() ?: run {
                JOptionPane.showMessageDialog(
                    InterfaceUI.instance,
                    "Invalid Component ID",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
                return
            }

            Copy.to(c.interfaceId)
                .startAt(compId)
                .addComponents(Consumer { comp ->
                    comp.parentId = -1
                    comp.version = 3
                    comp.type = ComponentType.SPRITE
                    comp.baseX = 52
                    comp.baseY = 42
                    comp.baseWidth = 24
                    comp.baseHeight = 24
                    comp.spriteId = 572
                    comp.optionMask = 2
                    comp.settings = IComponentSettings(2, -1)
                    comp.rightClickOptions = arrayOf("OP_NAME")
                    comp.onLoadScript = arrayOf(
                        6,
                        -2147483645,
                        "LAYER_COMPONENT_HASH",
                        "ICON_ON",
                        "ICON_OFF_ID",
                        "MAGIC_LEVEL_REQ",
                        "SPELL_NAME",
                        "SPELL_TOOLTIP",
                        "RUNE_ITEM_ID1",
                        "RUNE_ID1_AMOUNT_REQ",
                        "RUNE_ITEM_ID2",
                        "RUNE_ID2_AMOUNT_REQ",
                        "RUNE_ITEM_ID3",
                        "RUNE_ID3_AMOUNT_REQ",
                        -1,
                        0
                    )
                })
                .save()
            getInterface(c.interfaceId, true)
            refreshUI(c.interfaceId)
        }

    @JvmStatic
    fun createHighlightButton() {
        val c = InterfaceUI.selectedComponent
        if (c == null || c.interfaceId == -1) {
            JOptionPane.showMessageDialog(
                InterfaceUI.instance,
                "Please select an interface from the list first",
                "Warning",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        val defaultId = ComponentDefinition.getInterfaceDefinitionsComponentsSize(c.interfaceId)
        val input = JOptionPane.showInputDialog(
            InterfaceUI.instance,
            "Save as? (Component ID)",
            defaultId
        )

        val firstCompId = input?.toIntOrNull() ?: run {
            JOptionPane.showMessageDialog(
                InterfaceUI.instance,
                "Invalid Component ID",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
            return
        }
        Copy.to(c.interfaceId)
            .startAt(firstCompId)
            .addComponents(Consumer { comp ->
                comp.version = 3
                comp.parentId = -1
                comp.type = ComponentType.LAYER
                comp.baseX = 309
                comp.baseY = 57
                comp.baseWidth = 200
                comp.baseHeight = 200
                comp.onMouseLeaveScript = arrayOf(40, -2147483645, comp.parentId)
            })
            .save()
        val secondCompId = firstCompId + 1
        Copy.to(c.interfaceId)
            .startAt(secondCompId)
            .addComponents(Consumer { comp ->
                comp.version = 3
                comp.parentId = firstCompId
                comp.type = ComponentType.SPRITE
                comp.baseX = 150
                comp.baseY = 150
                comp.baseWidth = 42
                comp.baseHeight = 42
                comp.spriteId = 1058
                comp.rightClickOptions = arrayOf("OP_TEXT")
                comp.onMouseHoverScript = arrayOf(44, comp.parentId, "SPRITE_ID")
                comp.onMouseLeaveScript = arrayOf(299, firstCompId, -2147483645, "SPRITE_ID")
                comp.onMouseRepeat = arrayOf(38, -2147483645, firstCompId, "OP_TEXT", 25, 80)
            })
        .save()
        getInterface(c.interfaceId, true)
        refreshUI(c.interfaceId)
    }

    @JvmStatic
    fun createSimpleButton() {
        val c = InterfaceUI.selectedComponent
        if (c == null || c.interfaceId == -1) {
            JOptionPane.showMessageDialog(
                InterfaceUI.instance,
                "Please select an interface from the list first",
                "Warning",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        val defaultId = ComponentDefinition.getInterfaceDefinitionsComponentsSize(c.interfaceId)
        val input = JOptionPane.showInputDialog(
            InterfaceUI.instance,
            "Save as? (Component ID)",
            defaultId
        )

        val compId = input?.toIntOrNull() ?: run {
            JOptionPane.showMessageDialog(
                InterfaceUI.instance,
                "Invalid Component ID",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
            return
        }

        Copy.to(c.interfaceId)
            .startAt(compId)
            .addComponents(Consumer { comp ->
                comp.version = 3
                comp.type = ComponentType.LAYER
                comp.baseX = 140
                comp.baseY = 140
                comp.baseWidth = 50
                comp.baseHeight = 50
                comp.settings = IComponentSettings(0, -1)
                comp.onLoadScript = arrayOf(92, -2147483645)
                comp.onMouseHoverScript = arrayOf(94, -2147483645)
                comp.onMouseLeaveScript = arrayOf(92, -2147483645)
            })
            .save()

        getInterface(c.interfaceId, true)
        refreshUI(c.interfaceId)
    }

    @JvmStatic
    fun createCloseButton() {
        val c = InterfaceUI.selectedComponent
        if (c == null || c.interfaceId == -1) {
            JOptionPane.showMessageDialog(
                InterfaceUI.instance,
                "Please select an interface from the list first",
                "Warning",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        val defaultId = ComponentDefinition.getInterfaceDefinitionsComponentsSize(c.interfaceId)
        val input = JOptionPane.showInputDialog(
            InterfaceUI.instance,
            "Save as? (Component ID)",
            defaultId
        )

        val compId = input?.toIntOrNull() ?: run {
            JOptionPane.showMessageDialog(
                InterfaceUI.instance,
                "Invalid Component ID",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
            return
        }

        Copy.to(c.interfaceId)
            .startAt(compId)
            .addComponents(Consumer { comp ->
                comp.version = 3
                comp.type = ComponentType.SPRITE
                comp.baseX = 463
                comp.baseY = 33
                comp.baseWidth = 16
                comp.baseHeight = 16
                comp.spriteId = 831
                comp.optionMask = 2
                comp.settings = IComponentSettings(2, -1)
                comp.rightClickOptions = arrayOf("Close")
                comp.onMouseHoverScript = arrayOf(44, -2147483645, 832)
                comp.onMouseLeaveScript = arrayOf(44, -2147483645, 831)
                comp.onOptionClick = arrayOf(29)
            })
            .save()

        getInterface(c.interfaceId, true)
        refreshUI(c.interfaceId)
    }

    @JvmStatic
    fun createDualColorButton() {
        val c = InterfaceUI.selectedComponent
        if (c == null || c.interfaceId == -1) {
            JOptionPane.showMessageDialog(
                InterfaceUI.instance,
                "Please select an interface from the list first",
                "Warning",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        val defaultId = ComponentDefinition.getInterfaceDefinitionsComponentsSize(c.interfaceId)
        val input = JOptionPane.showInputDialog(
            InterfaceUI.instance,
            "Save as? (Component ID for SimpleButton)",
            defaultId
        )

        val firstCompId = input?.toIntOrNull() ?: run {
            JOptionPane.showMessageDialog(
                InterfaceUI.instance,
                "Invalid Component ID",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
            return
        }

        Copy.to(c.interfaceId)
            .startAt(firstCompId)
            .addComponents(Consumer { comp ->
                comp.version = 3
                comp.parentId = -1
                comp.type = ComponentType.LAYER
                comp.baseX = 90
                comp.baseY = 200
                comp.baseWidth = 100
                comp.baseHeight = 50
                comp.settings = IComponentSettings(0, -1)
                comp.onLoadScript = arrayOf(92, -2147483645)
                comp.onMouseHoverScript = arrayOf(94, -2147483645)
                comp.onMouseLeaveScript = arrayOf(92, -2147483645)
            })
            .save()

        val secondCompId = firstCompId + 1
        Copy.to(c.interfaceId)
            .startAt(secondCompId)
            .addComponents(Consumer { comp ->
                comp.version = 3
                comp.type = ComponentType.TEXT
                comp.baseWidth = 100
                comp.baseHeight = 50
//              comp.textLineHeight = 12
                comp.parentId = firstCompId
                comp.shadow = true
                comp.text = "OP_NAME"
                comp.textVerticalAli = 1
                comp.textHorizontalAli = 1
                comp.fontId = 495
                comp.color = 52224
                comp.settings = IComponentSettings(0, -1)
                comp.onMouseHoverScript = arrayOf(45, -2147483645, "COLOR_OFF")
                comp.onMouseLeaveScript = arrayOf(45, -2147483645, "COLOR_ON")
            })
            .save()
        getInterface(c.interfaceId, true)
        refreshUI(c.interfaceId)
    }
}