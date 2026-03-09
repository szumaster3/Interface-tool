package rs2.editor.model;

import com.alex.loaders.mesh.Unknown2;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Properties;

public class ModelViewer extends JFrame implements Runnable {
    public static boolean isOptimizedSelected = false;
    private static long gcDelay = 0L;
    private static ModelViewerRenderer r;
    private volatile boolean running = true;

    JFileChooser fileChooser2;
    private int posY = 159;
    private int posX = 268;
    private int axisX = 0;
    private int axisY = 0;
    private int axisZ = 500;
    private int screenX = -1;
    private int screenY = -1;
    private String directoryPath = "";
    private Unknown2 model;
    private final JFileChooser fileChooser1;
    private final JList<Object> field44;
    private final JMenu field45;
    private final JMenuBar field46;
    private final JMenuItem field47;
    private final JScrollPane field48;
    private final JMenuItem field49;
    private final JMenuItem field50;
    private final JCheckBox optimizedRendering;
    private final JPanel modelViewerPanel;

    public ModelViewer() {
        this.fileChooser1 = new JFileChooser();
        this.fileChooser2 = new JFileChooser();
        this.modelViewerPanel = new JPanel();
        this.field48 = new JScrollPane();
        this.field44 = new JList<>();
        this.optimizedRendering = new JCheckBox();
        this.field46 = new JMenuBar();
        this.field45 = new JMenu();
        this.field50 = new JMenuItem();
        this.field49 = new JMenuItem();
        this.field47 = new JMenuItem();
        this.setTitle("Model");

        ModelViewer modelViewer = this;
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                modelViewer.setState(Frame.ICONIFIED);
            }
        });

        this.modelViewerPanel.setBackground(new Color(255, 255, 255, 0));
        this.modelViewerPanel.addMouseWheelListener(new EditorMouseWheelListener(this));
        this.modelViewerPanel.addMouseListener(new EditorMouseAdapter(this));
        this.modelViewerPanel.addComponentListener(new EditorComponentAdapter(this));
        this.modelViewerPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent event) {
                modelViewer.mouseDragged(modelViewer, event);
            }
        });

        GroupLayout var2 = new GroupLayout(this.modelViewerPanel);
        this.modelViewerPanel.setLayout(var2);
        var2.setHorizontalGroup(var2.createParallelGroup(Alignment.LEADING).addGap(0, 536, 32767));
        var2.setVerticalGroup(var2.createParallelGroup(Alignment.LEADING).addGap(0, 318, 32767));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(field48, BorderLayout.CENTER);

        var2.setHorizontalGroup(
                var2.createParallelGroup(Alignment.LEADING)
                        .addGroup(var2.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(leftPanel, -2, 133, -2)
                                .addGap(18, 18, 18)
                                .addGroup(var2.createParallelGroup(Alignment.LEADING)
                                        .addComponent(this.optimizedRendering)
                                        .addComponent(this.modelViewerPanel, -1, -1, 32767))
                                .addContainerGap())
        );

        var2.setVerticalGroup(
                var2.createParallelGroup(Alignment.LEADING)
                        .addGroup(Alignment.TRAILING, var2.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(var2.createParallelGroup(Alignment.TRAILING)
                                        .addGroup(Alignment.LEADING, var2.createSequentialGroup()
                                                .addComponent(this.optimizedRendering)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(this.modelViewerPanel, -1, -1, 32767))
                                        .addComponent(leftPanel, -1, 343, 32767))
                                .addContainerGap())
        );

        this.pack();
        SwingUtilities.updateComponentTreeUI(this);
    }

    @Override
    public void run() {
        while (running) {
            if (this.model != null) {
                try {
                    r.updateModel(this.model, posX, axisY, posY, axisX, axisZ);
                    r.updatePreview(0, 0, this.modelViewerPanel.getGraphics());
                    r.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(10L);
            } catch (InterruptedException ignored) {
            }

            if (System.currentTimeMillis() - gcDelay > 60000L) {
                System.gc();
                gcDelay = System.currentTimeMillis();
            }
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new ModelViewer().setVisible(true));
    }

    private static byte[] getBytesFromFile(String path) {
        try {
            File file = new File(path);
            byte[] bytes = new byte[(int) file.length()];
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            dis.readFully(bytes, 0, bytes.length);
            dis.close();
            return bytes;
        } catch (Exception e) {
            return null;
        }
    }

    public static void windowClosingCallback(ModelViewer viewer, WindowEvent event) {
        viewer.method11();
    }

    public static void mouseWheelMoved(ModelViewer viewer, MouseWheelEvent event) {
        int rotation = event.getWheelRotation();
        viewer.axisZ += rotation * 50;
    }

    @SuppressWarnings("static-access")
    public static void mouseReleasedCallback(ModelViewer viewer, MouseEvent event) {
        viewer.screenX = viewer.screenY = -1;
        int mouseType = event.getButton();
        if (mouseType == 3) {
            viewer.posY = 159;
            viewer.posX = 268;
            viewer.axisX = 0;
            viewer.axisY = 0;
            viewer.axisZ = 500;
        }
    }

    public static void componentResizedCallback(ModelViewer viewer, ComponentEvent event) {
        if (viewer.modelViewerPanel.getWidth() > 0 && viewer.modelViewerPanel.getHeight() > 0) {
            viewer.render();
        }
    }

    public static void listSelectionValueChanged(ModelViewer viewer, ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            Object fileName = viewer.field44.getModel().getElementAt(viewer.field44.getAnchorSelectionIndex());
            byte[] modelBytes;
            Unknown2 model;
            if ((modelBytes = getBytesFromFile(viewer.directoryPath + System.getProperty("file.separator") + fileName))[modelBytes.length - 1] == -1 &&
                    modelBytes[modelBytes.length - 2] == -1) {
                if ((model = Unknown2.loadModel(modelBytes)).isNewHeader) {
                    model.downScaleModel(4);
                }
            } else {
                Unknown2.initModel(modelBytes);
                model = Unknown2.createEmptyModel();
            }
            model.method38(64, 768, -50, -10, -50, true);
            viewer.model = model;
        }
    }

    public static void actionListenerCallback3(ModelViewer viewer, ActionEvent event) {
        isOptimizedSelected = viewer.optimizedRendering.isSelected();
    }

    public static void actionListenerCallback(ModelViewer viewer, ActionEvent event) {
        viewer.fileChooser1.setFileSelectionMode(JFileChooser.FILES_ONLY);
        File file;
        if (viewer.fileChooser1.showOpenDialog(viewer) == JFileChooser.APPROVE_OPTION &&
                (file = viewer.fileChooser1.getSelectedFile()).isFile()) {
            byte[] modelBytes;
            Unknown2 model;
            if ((modelBytes = getBytesFromFile(file.getAbsolutePath()))[modelBytes.length - 1] == -1 &&
                    modelBytes[modelBytes.length - 2] == -1) {
                if ((model = Unknown2.loadModel(modelBytes)).isNewHeader) {
                    model.downScaleModel(4);
                }
            } else {
                Unknown2.initModel(modelBytes);
                model = Unknown2.createEmptyModel();
            }
            model.method38(64, 768, -50, -10, -50, true);
            viewer.model = model;
        }
    }

    @SuppressWarnings("unchecked")
    public static void actionListenerCallback2(ModelViewer viewer, ActionEvent event) {
        viewer.fileChooser2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File folder;
        if (viewer.fileChooser2.showOpenDialog(viewer) == JFileChooser.APPROVE_OPTION &&
                (folder = viewer.fileChooser2.getSelectedFile()).isDirectory()) {
            viewer.directoryPath = folder.getAbsolutePath();
            File[] files = folder.listFiles();
            if (files != null) {
                String[] fileNames = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    fileNames[i] = files[i].getName();
                }
                viewer.field44.setModel(new EditorAbstractListModel(viewer, files.length, fileNames));
            }
            viewer.method11();
        }
    }

    public final void render() {
        r = new ModelViewerRenderer(this.modelViewerPanel.getWidth(), this.modelViewerPanel.getHeight(), this.modelViewerPanel);
    }

    @SuppressWarnings("unchecked")
    public final void loadFiles() {
        try {
            FileInputStream fis = new FileInputStream("");
            Properties props = new Properties();
            props.load(fis);
            String path = props.getProperty("path");
            String optimizeProp = props.getProperty("optimize");
            if (optimizeProp != null) {
                isOptimizedSelected = Boolean.parseBoolean(optimizeProp);
            }
            this.optimizedRendering.setSelected(isOptimizedSelected);

            if (path != null) {
                this.directoryPath = path;
                File dir = new File(this.directoryPath);
                if (dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        String[] fileNames = new String[files.length];
                        for (int i = 0; i < files.length; i++) {
                            fileNames[i] = files[i].getName();
                        }
                        this.field44.setModel(new EditorAbstractListModel2(this, files.length, fileNames));
                    }
                }
            }
            fis.close();
        } catch (Exception ignored) {
        }
    }

    private void method11() {
        Properties props = new Properties();
        props.setProperty("path", this.directoryPath);
        props.setProperty("optimize", Boolean.toString(isOptimizedSelected));
    }

    @SuppressWarnings("static-access")
    public void mouseDragged(ModelViewer viewer, MouseEvent event) {
        int x = event.getX();
        int y = event.getY();
        String button = event.getMouseModifiersText(event.getModifiersEx());
        int mouseType = Character.getNumericValue(button.charAt(button.length() - 1));

        if (mouseType == 1) {
            if (viewer.screenX != -1 || viewer.screenY != -1) {
                viewer.axisX += (viewer.screenX - x) * 3;
                viewer.axisX = Math.floorMod(viewer.axisX, 2048);

                viewer.axisY -= (viewer.screenY - y) * 3;
                viewer.axisY = Math.floorMod(viewer.axisY, 2048);
            }
        } else if (mouseType == 2) {
            if (viewer.screenX != -1 || viewer.screenY != -1) {
                viewer.posX += x - viewer.screenX;
                viewer.posX = Math.floorMod(viewer.posX, 2048);

                viewer.posY -= viewer.screenY - y;
                viewer.posY = Math.floorMod(viewer.posY, 2048);
            }
        }

        viewer.screenX = x;
        viewer.screenY = y;
    }
}