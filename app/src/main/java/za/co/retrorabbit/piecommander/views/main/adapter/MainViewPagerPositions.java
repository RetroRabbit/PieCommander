package za.co.retrorabbit.piecommander.views.main.adapter;

import android.support.annotation.StringRes;

import java.util.HashMap;
import java.util.Map;

import za.co.retrorabbit.piecommander.R;

/**
 * Created by Herman Barnardt on 2016-01-20.
 */
public enum MainViewPagerPositions {
    DEVICES(0, R.string.main_tab_text_2),
    CONTROLS(1, R.string.main_tab_text_1);

    private static Map<Integer, MainViewPagerPositions> map = new HashMap<Integer, MainViewPagerPositions>();

    static {
        for (MainViewPagerPositions cardType : MainViewPagerPositions.values()) {
            map.put(cardType.value, cardType);
        }
    }

    private final int value;
    private final int titleRes;

    MainViewPagerPositions(int value, @StringRes int titleRes) {
        this.value = value;
        this.titleRes = titleRes;
    }

    public static MainViewPagerPositions getValueOf(int value) {
        return map.containsKey(value) ? map.get(value) : CONTROLS;
    }

    public int getValue() {
        return value;
    }

    public
    @StringRes
    int getTitleRes() {
        return titleRes;
    }

}
