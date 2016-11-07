package za.co.retrorabbit.piecommander;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import za.co.retrorabbit.piecommander.fragments.ControlsFragment;
import za.co.retrorabbit.piecommander.fragments.DeviceFragment;
import za.co.retrorabbit.piecommander.fragments.dummy.DummyContent;

public class MainActivity extends AppCompatActivity implements ControlsFragment.OnFragmentInteractionListener, DeviceFragment.OnListFragmentInteractionListener {

    @BindView(R.id.activity_main_view_pager)
    ViewPager viewPager;

    @BindView(R.id.activity_main_tab_layout)
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        viewPager.setAdapter(new MainTabAdapter(getSupportFragmentManager(), this));
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }
}
