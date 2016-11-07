package za.co.retrorabbit.piecommander;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import za.co.retrorabbit.piecommander.fragments.ControlsFragment;
import za.co.retrorabbit.piecommander.fragments.DeviceFragment;

/**
 * Created by Bernhard Müller on 10/6/2016.
 */

public class MainTabAdapter extends FragmentStatePagerAdapter {
    private Context context;

    public MainTabAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return MainViewPagerPositions.values().length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return context.getString(MainViewPagerPositions.getValueOf(position).getTitleRes());
    }

    @Override
    public Fragment getItem(int position) {
        switch (MainViewPagerPositions.getValueOf(position)) {
            case CONTROLS:
                return ControlsFragment.newInstance();
            case DEVICES:
                return DeviceFragment.newInstance();
            default:
                return ControlsFragment.newInstance();
        }
    }
}