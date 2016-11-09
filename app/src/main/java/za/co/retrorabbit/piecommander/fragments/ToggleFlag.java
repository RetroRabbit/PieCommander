package za.co.retrorabbit.piecommander.fragments;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wsche on 2016/11/09.
 */
public enum ToggleFlag {
    UNSET(0),
    LEFT_LASER(2),
    RIGHT_LASER(3),
    LED_RED(4),
    LED_GREEN(5),
    LED_BLUE(6);


    private static Map<Integer, ToggleFlag> map = new HashMap<Integer, ToggleFlag>();

    static {
        for (ToggleFlag toggleFlag : ToggleFlag.values()) {
            map.put(toggleFlag.value, toggleFlag);
        }
    }

    private final int value;

    ToggleFlag(int value) {
        this.value = value;
    }

    public static ToggleFlag getType(int value) {
        return map.containsKey(value) ? map.get(value) : UNSET;
    }

    public int getValue() {
        return value;
    }
}
