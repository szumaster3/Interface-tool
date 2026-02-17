package fs.com.alex.utils;

import rs.cache.ComponentDefinition;
import rs.cache.ComponentType;
import fs.com.alex.io.InputStream;
import fs.com.alex.io.OutputStream;

import java.awt.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

import static java.lang.Math.max;

public final class Utils {

    private Utils() {
    }

    public static byte[] cryptRSA(byte[] data, BigInteger exponent, BigInteger modulus) {
        return new BigInteger(data).modPow(exponent, modulus).toByteArray();
    }

    public static String getJagString(InputStream stream) {
        int first = stream.readUnsignedByte();
        if (first == 0) {
            return "";
        }
        return (char) first + getString(stream);
    }

    private static String getString(InputStream stream) {
        StringBuilder bldr = new StringBuilder();
        int b;
        while (true) {
            b = stream.readUnsignedByte();
            if (b == 0) break;
            bldr.append((char) b);
        }
        return bldr.toString();
    }

    public static byte[] getArchivePacketData(int indexId, int archiveId, byte[] archive) {
        OutputStream stream = new OutputStream(archive.length + 4);

        stream.writeByte(indexId);
        stream.writeShort(archiveId);
        stream.writeByte(0);
        stream.writeInt(archive.length);

        int offset = 8;

        for (int i = 0; i < archive.length; i++) {
            if (offset == 512) {
                stream.writeByte(-1);
                offset = 1;
            }

            stream.writeByte(archive[i]);
            offset++;
        }

        byte[] data = new byte[stream.getOffset()];
        stream.setOffset(0);
        stream.getBytes(data, 0, data.length);
        return data;
    }

    public static int getMediumInt(ByteBuffer buffer) {
        return ((buffer.get() & 0xFF) << 16) | ((buffer.get() & 0xFF) << 8) | (buffer.get() & 0xFF);
    }

    public static void putMediumInt(ByteBuffer buffer, int value) {
        buffer.put((byte) (value >> 16));
        buffer.put((byte) (value >> 8));
        buffer.put((byte) value);
    }

    public static int getNameHash(String name) {
        return name.toLowerCase(Locale.getDefault()).hashCode();
    }

    public static ComponentDefinition getParent(int parentHash) {
        if (parentHash == -1) return null;

        Set<Integer> visited = new HashSet<>();
        int currentHash = parentHash;

        while (currentHash != -1) {
            if (!visited.add(currentHash)) {
                return null;
            }

            int interfaceId = currentHash >>> 16;
            int baseHash = interfaceId << 16;
            int componentId = currentHash - baseHash;

            ComponentDefinition comp = ComponentDefinition.getInterfaceComponent(interfaceId, componentId);
            if (comp == null) return null;

            currentHash = comp.getParentId();
        }

        return null;
    }

    public static List<ComponentDefinition> getChildren(int parentHash) {
        ComponentDefinition parent = getParent(parentHash);
        if (parent == null) return new ArrayList<>();
        int interfaceId = parentHash >>> 16;
        List<ComponentDefinition> result = new ArrayList<>();
        for (ComponentDefinition comp : getAllComponents(interfaceId)) {
            if (comp.getParentId() == parentHash) {
                result.add(comp);
            }
        }
        return result;
    }

    public static Point computeAbsolutePosition(ComponentDefinition comp) {
        if (comp == null) return new Point(0, 0);

        int ax = comp.getPositionX();
        int ay = comp.getPositionY();

        ComponentDefinition parent = getParent(comp.getParentId());
        while (parent != null) {
            ax += parent.getPositionX();
            ay += parent.getPositionY();
            parent = getParent(parent.getParentId());
        }

        return new Point(ax, ay);
    }

    public static boolean isHidden(ComponentDefinition component) {
        if (component.getHidden()) return true;
        ComponentDefinition parent = getParent(component.getParentId());
        while (parent != null) {
            if (parent.getHidden()) return true;
            parent = getParent(parent.getParentId());
        }
        return false;
    }

    public static Object[] getScriptArray(String input) {
        if (input == null || input.isEmpty()) return null;
        String[] parts = input.split(";");
        List<String> values = new ArrayList<>();
        for (String s : parts) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) values.add(trimmed);
        }
        if (values.isEmpty()) return null;

        Object[] objs = new Object[values.size()];
        try {
            objs[0] = Integer.parseInt(values.get(0));
        } catch (NumberFormatException e) {
            return null;
        }

        for (int i = 1; i < values.size(); i++) {
            try {
                objs[i] = Integer.parseInt(values.get(i));
            } catch (NumberFormatException e) {
                objs[i] = values.get(i);
            }
        }
        return objs;
    }

    public static int[] getConfigArray(String input) {
        if (input == null || input.isEmpty()) return null;
        String[] parts = input.split(";");
        List<String> values = new ArrayList<>();
        for (String s : parts) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) values.add(trimmed);
        }
        if (values.isEmpty()) return null;

        int[] result = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            try {
                result[i] = Integer.parseInt(values.get(i));
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        return result;
    }

    public static String getComponentType(int type) {
        switch (type) {
            case ComponentType.LAYER:
                return "Layer";
            case ComponentType.TEXT:
                return "Text";
            case ComponentType.SPRITE:
                return "Sprite";
            case ComponentType.FIGURE:
                return "Figure";
            case ComponentType.MODEL:
                return "Model";
            case ComponentType.LINE:
                return "Line";
            case ComponentType.MODEL_LIST:
                return "Model List";
            case ComponentType.INVENTORY:
                return "Inventory";
            default:
                return Integer.toString(type);
        }
    }

    public static void drawSelectedComponent(Graphics g, ComponentDefinition comp, boolean fill) {
        java.awt.Point abs = computeAbsolutePosition(comp);
        int x = abs.x;
        int y = abs.y;
        int width = max(1, comp.getWidth());
        int height = max(1, comp.getHeight());

        Color fillColor = null;
        switch (comp.getType()) {
            case ComponentType.SPRITE:
                fillColor = new Color(127, 80, 190, 70);
                break;
            case ComponentType.FIGURE:
                fillColor = new Color(110, 110, 110, 70);
                break;
            case ComponentType.TEXT:
                fillColor = new Color(255, 251, 0, 70);
                break;
            case ComponentType.MODEL:
                fillColor = new Color(87, 87, 210, 70);
                break;
            case ComponentType.LAYER:
                fillColor = new Color(0, 174, 0, 70);
                break;
        }

        Color outlineColor;
        switch (comp.getType()) {
            case ComponentType.SPRITE:
                outlineColor = new Color(141, 86, 211);
                break;
            case ComponentType.FIGURE:
                outlineColor = new Color(110, 110, 110);
                break;
            case ComponentType.TEXT:
                outlineColor = new Color(255, 251, 0);
                break;
            case ComponentType.MODEL:
                outlineColor = new Color(87, 87, 210);
                break;
            case ComponentType.LAYER:
                outlineColor = new Color(0, 174, 0);
                break;
            default:
                outlineColor = Color.YELLOW;
        }

        Graphics2D g2 = (Graphics2D) g;
        if (fill && fillColor != null) {
            g2.setColor(fillColor);
            g2.fillRect(x, y, width, height);
        }

        g2.setColor(outlineColor);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(x, y, width, height);
    }

    public static List<ComponentDefinition> getAllComponents(int interfaceId) {
        List<ComponentDefinition> list = new ArrayList<>();
        int size = ComponentDefinition.getInterfaceDefinitionsComponentsSize(interfaceId);
        for (int i = 0; i < size; i++) {
            ComponentDefinition c = ComponentDefinition.getInterfaceComponent(interfaceId, i);
            if (c != null) list.add(c);
        }
        return list;
    }

    public static boolean isScrollBar(ComponentDefinition component) {
        if (component == null) return false;
        if (component.getBaseWidth() > 16) return false;

        if (component.getOnLoadScript() == null || component.getOnLoadScript().length == 0) {
            return false;
        }

        try {
            Object firstObj = component.getOnLoadScript()[0];
            int first = Integer.parseInt(firstObj.toString());
            return first == 30 || first == 62 || first == 63;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isButton(ComponentDefinition component) {
        if (component == null) return false;
        if (component.getOnLoadScript() == null || component.getOnLoadScript().length == 0) {
            return false;
        }

        try {
            Object firstObj = component.getOnLoadScript()[0];
            int first = Integer.parseInt(firstObj.toString());
            return first == 92 || first == 93;
        } catch (Exception e) {
            return false;
        }
    }
}
