package com.alex.loaders.interfaces;

/**
 * @author paolo #Shnek6969
 * @since 04/08/2019
 */
public class ContainerHelper {
    public static boolean isScrollBar(ComponentDefinition component) {
        return component.getOnLoadScript() != null && Integer.parseInt(component.getOnLoadScript()[0].toString()) == 30 && component.getBaseWidth() < 32;
    }

    public static boolean isButton(ComponentDefinition component) {
        return component.getOnLoadScript() != null && Integer.parseInt(component.getOnLoadScript()[0].toString()) == 92;
    }
}