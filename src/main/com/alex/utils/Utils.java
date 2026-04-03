package com.alex.utils;

import com.alex.io.InputStream;
import com.alex.io.OutputStream;
import com.alex.loaders.interfaces.ComponentDefinition;
import com.alex.loaders.interfaces.ComponentType;
import rs2.editor.ui.Consts;

import java.awt.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.*;

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

    public static int readMedium(ByteBuffer buffer) {
        return ((buffer.get() & 0xFF) << 16) | ((buffer.get() & 0xFF) << 8) | (buffer.get() & 0xFF);
    }

    public static void writeMedium(ByteBuffer buffer, int value) {
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

    public static String getComponentFromInt(int type)
    {
        switch (type) {
            case ComponentType.LAYER:
                return "layer";
            case ComponentType.TEXT:
                return "text";
            case ComponentType.SPRITE:
                return "sprite";
            case ComponentType.FIGURE:
                return "figure";
            case ComponentType.MODEL:
                return "model";
            case ComponentType.LINE:
                return "line";
            case ComponentType.MODEL_LIST:
                return "model list";
            case ComponentType.INVENTORY:
                return "inventory";
            default:
                return Integer.toString(type);
        }
    }

    public static int getComponentFromString(String name)
    {
        if (name == null) return -1;

        switch (name)
        {
            case "layer":        return ComponentType.LAYER;
            case "text":         return ComponentType.TEXT;
            case "sprite":       return ComponentType.SPRITE;
            case "figure":       return ComponentType.FIGURE;
            case "model":        return ComponentType.MODEL;
            case "line":         return ComponentType.LINE;
            case "model list":   return ComponentType.MODEL_LIST;
            case "inventory":    return ComponentType.INVENTORY;
            default:
                try
                {
                    return Integer.parseInt(name);
                }
                catch (NumberFormatException e) {
                    return -1;
                }
        }
    }
}
