package com.doublehammerstudios.intellitank.Adapters.ViewPager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.doublehammerstudios.intellitank.Fragments.Control.FeedingControlFragment;
import com.doublehammerstudios.intellitank.Fragments.Control.FilteringControlFragment;
import com.doublehammerstudios.intellitank.Fragments.Control.MainControlSystemFragment;

import java.util.ArrayList;
import java.util.List;

public class ControlTabViewPagerAdapter extends FragmentStateAdapter {

    public ControlTabViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new MainControlSystemFragment();
            case 1:
                return new FilteringControlFragment();
            case 2:
                return new FeedingControlFragment();
            default:
                return new MainControlSystemFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
