package util;

import rs.cache.ComponentDefinition;

/**
 * @author paolo #Shnek6969
 * @since 04/08/2019
 */
public class ContainerHelper {
    public static boolean isScrollBar(ComponentDefinition component) {
        if (component.getOnLoadScript() != null && Integer.parseInt(component.getOnLoadScript()[0].toString()) == 30)
            return true;
        return false;
    }

    public static boolean isButton(ComponentDefinition component) {
        if (component.getOnLoadScript() != null && Integer.parseInt(component.getOnLoadScript()[0].toString()) == 92)
            return true;
        return false;
    }
}