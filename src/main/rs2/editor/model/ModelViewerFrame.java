package rs2.editor.model;

import com.alex.loaders.mesh.ModelData;
import com.alex.loaders.mesh.Unknown2;
import com.alex.loaders.mesh.Unknown4;
import com.alex.store.Index;
import com.alex.store.Store;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

        setTitle("Model Viewer");
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
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setPreferredSize(new Dimension(200, 0));
        mainPanel.add(controlPanel, BorderLayout.WEST);

        listModel = new JList<>(listModelM);
        JScrollPane scrollPaneModel = new JScrollPane(listModel);
        scrollPaneModel.setPreferredSize(new Dimension(200, 450));
        controlPanel.add(scrollPaneModel);

        JPanel buttonsPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        controlPanel.add(buttonsPanel);

        txtModel = new JTextField();
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
        buttonsPanel.add(txtModel);
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
}