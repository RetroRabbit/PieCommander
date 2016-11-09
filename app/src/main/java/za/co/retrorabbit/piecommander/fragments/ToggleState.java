package za.co.retrorabbit.piecommander.fragments;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wsche on 2016/11/09.
 */
public enum ToggleState {
    ON(1),
    OFF(2),
    FLASH(3);

    private static Map<Integer, ToggleState> map = new HashMap<Integer, ToggleState>();

    static {
        for (ToggleState toggleState : ToggleState.values()) {
            map.put(toggleState.value, toggleState);
        }
    }

    private final int value;

    ToggleState(int value) {
        this.value = value;
    }

    public static ToggleState getType(int value) {
        return map.containsKey(value) ? map.get(value) : OFF;
    }

    public int getValue() {
        return value;
    }
}
