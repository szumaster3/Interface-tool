package rs2.editor.model

import com.alex.store.Store
import java.awt.Component
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import javax.swing.*

class MultiModelPacker : JFrame() {

    companion object {
        private var STORE: Store? = null
    }

    private lateinit var exit: JMenuItem
    private lateinit var jLabel1: JLabel
    private lateinit var jLabel2: JLabel
    private lateinit var jMenu1: JMenu
    private lateinit var jMenuBar1: JMenuBar
    private lateinit var modelDir: JMenuItem
    private lateinit var modelDirField: JTextField
    private lateinit var sameId: JCheckBox
    private lateinit var submit: JButton

    fun showWindow(cache: String) {
        try {
            STORE = Store(cache)
        } catch (_: Exception) {

        }

        initComponents()
        isResizable = false
        title = "MultiModelPacker"
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null as Component?)
        isVisible = true
    }

    private fun initComponents() {
        jLabel1 = JLabel("Multiple Model Packer")
        jLabel2 = JLabel("Models Directory")
        modelDirField = JTextField()
        sameId = JCheckBox("Keep Same ID").apply { toolTipText = "Keeps same ID as named" }
        submit = JButton("Submit")
        jMenuBar1 = JMenuBar()
        jMenu1 = JMenu("File")
        modelDir = JMenuItem("Choose Model Dir")
        exit = JMenuItem("Exit")

        submit.addActionListener { submitActionPerformed() }
        modelDir.addActionListener { modelDirActionPerformed() }
        exit.addActionListener { exitActionPerformed() }

        jMenu1.add(modelDir)
        jMenu1.add(exit)
        jMenuBar1.add(jMenu1)
        jMenuBar = jMenuBar1

        val layout = GroupLayout(contentPane)
        contentPane.layout = layout
        layout.autoCreateGaps = true
        layout.autoCreateContainerGaps = true

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(modelDirField)
                .addGroup(
                    layout.createSequentialGroup()
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel2)
                                .addComponent(sameId)
                        )
                )
                .addGroup(layout.createSequentialGroup().addComponent(jLabel1))
                .addGroup(layout.createSequentialGroup().addComponent(submit))
        )

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(10)
                .addComponent(jLabel2)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modelDirField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18)
                .addComponent(sameId)
                .addGap(10)
                .addComponent(submit)
        )

        pack()
    }

    private fun submitActionPerformed() {
        val directory = modelDirField.text
        val keepID = sameId.isSelected

        File(directory).listFiles()?.forEach { file ->
            if (file.isFile) {
                try {
                    if (keepID) {
                        val idStr = file.name.replace(".dat", "")
                        STORE?.let { packCustomModel(it, getBytesFromFile(file), idStr.toInt()) }
                    } else {
                        STORE?.let { packCustomModel(it, getBytesFromFile(file)) }
                    }
                } catch (_: IOException) {
                }
            }
        }
    }

    private fun modelDirActionPerformed() {
        val fc = JFileChooser()
        fc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        val returnVal = fc.showOpenDialog(this)
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            val file = fc.selectedFile
            modelDirField.text = file.path + "/"
        }
    }

    private fun exitActionPerformed() {
        dispose()
    }

    fun packCustomModel(cache: Store, data: ByteArray?): Int {
        val archiveId = cache.indexes[7].lastArchiveId + 1
        if (cache.indexes[7].putFile(archiveId, 0, data)) {
            return archiveId
        } else {
            println("Failed packing model $archiveId")
            return -1
        }
    }

    fun packCustomModel(cache: Store, data: ByteArray?, modelId: Int): Int {
        if (cache.indexes[7].putFile(modelId, 0, data)) {
            return modelId
        } else {
            println("Failed packing model $modelId")
            return -1
        }
    }

    @Throws(IOException::class)
    fun getBytesFromFile(file: File): ByteArray {
        val length = file.length()
        if (length > Int.MAX_VALUE) {
            throw IOException("File is too large: ${file.name}")
        }

        FileInputStream(file).use { fis ->
            val bytes = ByteArray(length.toInt())
            var offset = 0
            var numRead: Int

            while (offset < bytes.size) {
                numRead = fis.read(bytes, offset, bytes.size - offset)
                if (numRead < 0) break
                offset += numRead
            }

            if (offset < bytes.size) {
                throw IOException("Could not completely read file ${file.name}")
            }

            return bytes
        }
    }
}
