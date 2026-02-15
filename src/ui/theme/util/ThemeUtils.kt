package ui.theme.util

import ui.theme.assets.SoundLibrary
import java.awt.Component
import javax.swing.JOptionPane

object ThemeUtils {
    fun showError(
        root: Component,
        message: String,
        title: String,
    ) {
        ThemeSoundsUtils.playSFX(SoundLibrary.ERROR_OPTION_SFX)
        JOptionPane.showMessageDialog(root, message, title, JOptionPane.ERROR_MESSAGE)
    }

    fun showWarning(
        root: Component,
        message: String,
        title: String,
    ) {
        ThemeSoundsUtils.playSFX(SoundLibrary.WARNING_OPTION_SFX)
        JOptionPane.showMessageDialog(root, message, title, JOptionPane.WARNING_MESSAGE)
    }

    fun showInfo(
        root: Component,
        message: String,
        title: String,
    ) {
        ThemeSoundsUtils.playSFX(SoundLibrary.INFO_OPTION_SFX)
        JOptionPane.showMessageDialog(root, message, title, JOptionPane.INFORMATION_MESSAGE)
    }

    fun showQuestion(
        root: Component,
        message: String,
        title: String,
    ): Int {
        ThemeSoundsUtils.playSFX(SoundLibrary.QUESTION_OPTION_SFX)
        return JOptionPane.showConfirmDialog(root, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
    }

    fun showConfirm(
        root: Component,
        message: String,
        title: String,
    ): Int {
        ThemeSoundsUtils.playSFX(SoundLibrary.QUESTION_OPTION_SFX)
        return JOptionPane.showConfirmDialog(root, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)
    }

    fun showInput(
        root: Component,
        message: String,
        title: String,
    ): String? {
        ThemeSoundsUtils.playSFX(SoundLibrary.QUESTION_OPTION_SFX)
        return JOptionPane.showInputDialog(root, message, title, JOptionPane.QUESTION_MESSAGE)
    }
}
