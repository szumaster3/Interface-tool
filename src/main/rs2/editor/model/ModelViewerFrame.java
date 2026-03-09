package rs2.editor.model;

import com.alex.loaders.mesh.ModelData;
import com.alex.loaders.mesh.Unknown2;
import com.alex.loaders.mesh.Unknown4;
import com.alex.store.Index;
import com.alex.store.Store;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModelViewerFrame extends JFrame {

    public static Store cache;

    private JTextField txtModel;
    private final DefaultListModel<Integer> listModelM = new DefaultListModel<>();
    private JList<Integer> listModel = new JList<>(listModelM);
    private AppPanel appPanel;
    private Thread modelViewThread;

    private final JTabbedPane tabbedPane;
    private final List<Integer> allModelIds = new ArrayList<>();

    public ModelViewerFrame(Store store) throws IOException {
        cache = store;

        setTitle("Media manager");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        loadCache(store);

        JPanel cachePanel = createCachePanel();
        tabbedPane.addTab("Model ids", cachePanel);
    }

    private void loadCache(Store store) {
        listModelM.clear();
        allModelIds.clear();

        Index index7 = store.getIndexes()[7];
        if (index7 == null) return;

        int lastId = index7.getLastArchiveId();
        for (int id = 0; id <= lastId; id++) {
            if (!index7.archiveExists(id)) continue;
            allModelIds.add(id);
            listModelM.addElement(id);
        }
    }

    private JPanel createCachePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(1, 1));

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setPreferredSize(new Dimension(200, 0));
        mainPanel.add(controlPanel, BorderLayout.WEST);

        listModel = new JList<>(listModelM);
        JScrollPane scrollPaneModel = new JScrollPane(listModel);
        scrollPaneModel.setPreferredSize(new Dimension(200, 400));
        controlPanel.add(scrollPaneModel);
        txtModel = new JTextField();
        txtModel.setPreferredSize(new Dimension(200, 25));
        txtModel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        txtModel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String filter = txtModel.getText().trim().toLowerCase();
                listModelM.clear();
                for (Integer id : allModelIds) {
                    if (String.valueOf(id).toLowerCase().contains(filter)) {
                        listModelM.addElement(id);
                    }
                }
            }
        });
        controlPanel.add(txtModel);

        JPanel buttonPanel = getjPanel();
        controlPanel.add(buttonPanel);

        appPanel = new AppPanel();
        JScrollPane modelScrollPane = new JScrollPane(appPanel);
        modelScrollPane.setPreferredSize(new Dimension(350, 400));
        mainPanel.add(modelScrollPane, BorderLayout.CENTER);

        listModel.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Integer selectedId = listModel.getSelectedValue();
                if (selectedId == null) return;

                if (!appPanel.getLoaded()) {
                    appPanel.setLoaded(true);
                    modelViewThread = new Thread(appPanel);
                    modelViewThread.start();
                    Unknown2.load();
                }
                Unknown2 model = Unknown4.getModel(selectedId);
                if (model != null && Unknown2.properties != null && Unknown2.properties.length > 0) {
                    ModelData prop = Unknown2.properties[0];
                    if (prop.data != null) {
                        appPanel.loadModelFromBytes(prop.data);
                    }
                }
            }
        });

        return mainPanel;
    }

    private @NotNull JPanel getjPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        JButton importButton = new JButton("Import");
        importButton.setPreferredSize(new Dimension(200, 25));
        importButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        importButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setMultiSelectionEnabled(true);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = chooser.getSelectedFiles();
                for (File file : selectedFiles) {
                    if (file.isDirectory()) {
                        File[] filesInDir = file.listFiles();
                        if (filesInDir != null) {
                            for (File f : filesInDir) {
                                try {
                                    byte[] data = getBytesFromFile(f);
                                    packCustomModel(cache, data);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    } else {
                        try {
                            byte[] data = getBytesFromFile(file);
                            packCustomModel(cache, data);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                JOptionPane.showMessageDialog(this, "Import completed!");
                loadCache(cache);
            }
        });

        JButton exportButton = new JButton("Export");
        exportButton.setPreferredSize(new Dimension(200, 25));
        exportButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        exportButton.addActionListener(e -> {
            int[] selectedIndices = listModel.getSelectedIndices();
            if (selectedIndices.length == 0) {
                JOptionPane.showMessageDialog(this, "No IDs selected to export!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File folder = fc.getSelectedFile();
                for (int idx : selectedIndices) {
                    Integer id = listModel.getModel().getElementAt(idx);
                    File out = new File(folder, id + ".dat");
                    try {
                        byte[] data = cache.getIndexes()[7].getFile(id, 0);
                        if (data != null) {
                            try (FileOutputStream fos = new FileOutputStream(out)) {
                                fos.write(data);
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                JOptionPane.showMessageDialog(this, "Export completed!");
            }
        });

        buttonPanel.add(importButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        buttonPanel.add(exportButton);

        return buttonPanel;
    }

    private int packCustomModel(Store cache, byte[] data) {
        int archiveId = cache.getIndexes()[7].getLastArchiveId() + 1;
        if (cache.getIndexes()[7].putFile(archiveId, 0, data)) {
            return archiveId;
        } else {
            System.out.println("Failed packing model " + archiveId);
            return -1;
        }
    }

    private int packCustomModel(Store cache, byte[] data, int modelId) {
        if (cache.getIndexes()[7].putFile(modelId, 0, data)) {
            return modelId;
        } else {
            System.out.println("Failed packing model " + modelId);
            return -1;
        }
    }

    private byte[] getBytesFromFile(File file) throws IOException {
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            throw new IOException("File too large: " + file.getName());
        }
        FileInputStream fis = new FileInputStream(file);
        try {
            byte[] bytes = new byte[(int) length];
            int offset = 0;
            int numRead;
            while (offset < bytes.length && (numRead = fis.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
            return bytes;
        } finally {
            fis.close();
        }
    }
}